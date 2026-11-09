package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.posgw.common.documents.BaseSessionEvent
import it.pagopa.posgw.common.documents.v1.sessions.SessionView
import it.pagopa.posgw.common.documents.v1.sessions.data.*
import it.pagopa.posgw.common.documents.v1.sessions.events.*
import it.pagopa.posgw.common.documents.v1.sessions.events.SessionEventCode.*
import it.pagopa.posgw.common.domain.v1.sessions.EmptySession
import it.pagopa.posgw.common.domain.v1.sessions.Session
import it.pagopa.posgw.common.domain.v1.sessions.SessionStatus
import it.pagopa.posgw.common.domain.v1.sessions.pojos.BaseSession
import it.pagopa.posgw.common.repositories.redis.v1.PaymentSession
import it.pagopa.posgw.common.repositories.redis.v1.PaymentSessionTemplateWrapper
import it.pagopa.posgw.transactions.handler.queues.publisher.*
import it.pagopa.posgw.transactions.handler.repositories.SessionsEventStoreRepository
import it.pagopa.posgw.transactions.handler.repositories.SessionsViewRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

/**
 * Test controller to trigger data publication (queues, db, redis) for testing purposes
 *
 * TODO: It is not intended for production use and should be removed before deploying to production.
 */
@RestController
class TestController(
    val ecommerceSyncPublisher: EcommerceSyncPublisher,
    val ecommerceSyncRetryPublisher: EcommerceSyncRetryPublisher,
    val gecSyncPublisher: GecSyncPublisher,
    val gecSyncRetryPublisher: GecSyncRetryPublisher,
    val sessionExpirePublisher: SessionExpirePublisher,
    val sessionPollPublisher: SessionPollPublisher,
    val systemTelemetryPublisher: SystemTelemetryPublisher,
    val paymentSessionTemplateWrapper: PaymentSessionTemplateWrapper,
    val eventStoreRepository: SessionsEventStoreRepository<Any?>,
    val viewRepository: SessionsViewRepository
) {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun router(): RouterFunction<*> = router {
        accept(APPLICATION_JSON).nest {
            path("/publish").invoke(publishMessage)
            path("/sessions").nest {
                GET("/{sessionId}").invoke(getSession)
                POST("").invoke(postSession)
            }
            path("/debug/mongo").nest {
                GET("/{sessionId}").invoke(getMongoSession)
                POST("").invoke(postMongoSession)
            }
        }
    }

    val publishMessage: (ServerRequest) -> Mono<ServerResponse> = { req ->
        req.bodyToMono<Map<String, Any>>()
            .switchIfEmpty { Mono.just(mapOf()) }
            .flatMap {
                val message = it["message"] as String?
                val resource = it["resource"] as String?
                val properties = it["properties"] as Map<String, Any>? ?: mapOf()

                if (message.isNullOrBlank()) {
                    ServerResponse.ok()
                        .bodyValue(mapOf("result" to "You forgot the 'message' value in the body!"))
                } else {
                    when (resource) {
                        "posgw.cmd.ecommerce.sync" ->
                            ecommerceSyncPublisher.publish(message, properties)

                        "posgw.cmd.ecommerce.sync.retry" ->
                            ecommerceSyncRetryPublisher.publish(message, properties)

                        "posgw.cmd.gec.sync" -> gecSyncPublisher.publish(message, properties)
                        "posgw.cmd.gec.sync.retry" ->
                            gecSyncRetryPublisher.publish(message, properties)

                        "posgw.cmd.session.poll" ->
                            sessionPollPublisher.publish(message, properties)

                        "posgw.cmd.session.expire" ->
                            sessionExpirePublisher.publish(message, properties)

                        "posgw.evt.sys.telemetry" ->
                            systemTelemetryPublisher.publish(message, properties)

                        else ->
                            Mono.just(
                                "You forgot the 'resource' value in the body or the value is invalid!")
                    }.flatMap { s -> ServerResponse.ok().bodyValue(mapOf("result" to s)) }
                }
            }
            .doOnError {
                ServerResponse.ok().bodyValue(mapOf("result" to "Unable to create map: $it"))
            }
    }

    val getSession: (ServerRequest) -> Mono<ServerResponse> = { req ->
        paymentSessionTemplateWrapper
            .findById(req.pathVariable("sessionId"))
            .doOnNext { logger.info("Fetched session: {}", it) }
            .doOnError { logger.error("Error fetching session", it) }
            .flatMap { session ->
                ServerResponse.ok().bodyValue(session)
            }
            .switchIfEmpty {
                ServerResponse.notFound().build()
            }
    }

    val postSession: (ServerRequest) -> Mono<ServerResponse> = { _ ->
        Mono.just(
                PaymentSession(
                    sessionId = UUID.randomUUID().toString(),
                    status = "ACTIVE",
                    correlationId = UUID.randomUUID().toString(),
                    integrationType = PaymentSession.IntegrationType.LEGACY,
                    pos = PaymentSession.Pos(terminalId = "terminal123"),
                    transaction =
                        PaymentSession.Transaction(
                            id = UUID.randomUUID().toString(), amount = 1000),
                    lifecycle =
                        PaymentSession.LifeCycle(
                            createdAt = Instant.now(),
                            activationTimeoutAt = Instant.now().plusSeconds(3600),
                            authorizationTimeoutAt = Instant.now().plusSeconds(7200)),
                    routing =
                        PaymentSession.Routing(
                            ciNotificationUrl = "https://example.com/ciNotification",
                            layerPosPSPUrl = "https://example.com/layerPosPSP")))
            .flatMap { sessionDocument ->
                paymentSessionTemplateWrapper
                    .save(sessionDocument)
                    .doOnNext { saveResult ->
                        logger.info(
                            "Saved session: [{}], document: [{}]", saveResult, sessionDocument)
                    }
                    .flatMap { _ ->
                        ServerResponse.ok().bodyValue(sessionDocument)
                    }
            }
            .onErrorResume {
                logger.error("Error saving session", it)
                ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue(it.message.toString())
            }
    }

    // writes one session per flow (together all event types) with its view, then reads each back
    val postMongoSession: (ServerRequest) -> Mono<ServerResponse> = { _ ->
        Flux.fromIterable(debugFlows())
            .concatMap { flow ->
                @Suppress("UNCHECKED_CAST")
                eventStoreRepository
                    .saveAll(flow.events as List<BaseSessionEvent<Any?>>)
                    .then(viewRepository.save(flow.view()))
                    .then(readMongoSession(flow.sessionId))
                    .map {
                        mapOf<String, Any?>(
                            "flow" to flow.name, "expectedStatus" to flow.expectedStatus.name) + it
                    }
            }
            .collectList()
            .map { sessions ->
                mapOf(
                    "allStatusesMatch" to sessions.all { it["status"] == it["expectedStatus"] },
                    "sessions" to sessions)
            }
            .flatMap { ServerResponse.ok().bodyValue(it) }
            .onErrorResume(::mongoErrorResponse)
    }

    val getMongoSession: (ServerRequest) -> Mono<ServerResponse> = { req ->
        val sessionId = runCatching { UUID.fromString(req.pathVariable("sessionId")) }.getOrNull()
        if (sessionId == null) {
            ServerResponse.badRequest().bodyValue(mapOf("result" to "Invalid session id"))
        } else {
            Mono.defer { readMongoSession(sessionId) }
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .onErrorResume(::mongoErrorResponse)
        }
    }

    private fun readMongoSession(sessionId: UUID): Mono<Map<String, Any?>> =
        eventStoreRepository
            .findBySessionIdOrderByTimestampAsc(sessionId)
            .collectList()
            .zipWith(
                viewRepository
                    .findById(sessionId.toString())
                    .map<Map<String, Any?>> { view ->
                        mapOf(
                            "id" to view.id,
                            "status" to view.status,
                            "events" to view.events.map { it.summary() })
                    }
                    // not mapOf(): Kotlin's EmptyMap can't be serialized by Jackson in native
                    .defaultIfEmpty(mutableMapOf()))
            .map {
                // replays the events through posgw-common's session state machine
                val initial: Session = EmptySession()
                val session =
                    it.t1.fold(initial) { state, event ->
                        state.applyEvent(event as SessionEvent<*>)
                    }
                mapOf(
                    "sessionId" to sessionId.toString(),
                    "status" to (session as? BaseSession)?.status?.name,
                    "state" to session.javaClass.simpleName,
                    "events" to it.t1.map { event -> event.summary() },
                    "view" to it.t2)
            }

    // the concrete type shows whether the polymorphic read restored the right event class
    private fun BaseSessionEvent<*>.summary(): Map<String, Any?> =
        mapOf("type" to javaClass.simpleName, "eventCode" to eventCode)

    private class DebugFlow(
        val name: String,
        val expectedStatus: SessionStatus,
        val sessionId: UUID,
        val events: List<SessionEvent<*>>
    ) {
        fun view(): SessionView =
            SessionView(
                id = sessionId.toString(),
                correlationId = (events.first() as SessionInitializedEvent).data.correlationId,
                status = expectedStatus.name,
                createdAt = events.first().timestamp,
                updatedAt = events.last().timestamp,
                events = events)
    }

    // valid paths of specs/v1/state_machine.json in posgw-common
    private fun debugFlows(): List<DebugFlow> =
        listOf(
            debugFlow(
                "happy-path",
                SessionStatus.ECOMM_NOTIFIED,
                SESSION_INITIALIZED_EVT,
                DEBT_POSITION_CREATED_EVT,
                TRANSACTION_ACTIVATED_EVT,
                AUTH_REQUESTED_EVT,
                AUTH_OUTCOME_RECEIVED_EVT,
                CI_NOTIFIED_EVT,
                ECOMM_NOTIFIED_EVT),
            debugFlow(
                "recovered-retries",
                SessionStatus.ECOMM_NOTIFIED,
                SESSION_INITIALIZED_EVT,
                TRANSACTION_ACTIVATED_EVT,
                AUTH_REQUEST_ERROR_EVT,
                AUTH_REQUEST_RETRIED_EVT,
                AUTH_REQUESTED_EVT,
                AUTH_OUTCOME_RECEIVED_EVT,
                CI_NTF_ERROR_EVT,
                CI_NTF_RETRIED_EVT,
                CI_NOTIFIED_EVT,
                ECOMM_NTF_ERROR_EVT,
                ECOMM_NTF_RETRIED_EVT,
                ECOMM_NTF_EXPIRED_EVT,
                ECOMM_NOTIFIED_EVT),
            debugFlow(
                "reconciliation",
                SessionStatus.RECONCILED,
                SESSION_INITIALIZED_EVT,
                TRANSACTION_ACTIVATED_EVT,
                AUTH_REQUESTED_EVT,
                AUTH_OUTCOME_RECEIVED_EVT,
                CI_NTF_ERROR_EVT,
                CI_NTF_EXPIRED_EVT,
                EXPIRED_EVT,
                RECONCILED_EVT),
            debugFlow(
                "terminal-error",
                SessionStatus.ERROR,
                SESSION_INITIALIZED_EVT,
                TRANSACTION_ACTIVATED_EVT,
                AUTH_REQUEST_ERROR_EVT,
                ERROR_EVT))

    // events 1s apart: Mongo keeps millisecons precision and the read orders by timestamp
    private fun debugFlow(
        name: String,
        expectedStatus: SessionStatus,
        vararg codes: SessionEventCode
    ): DebugFlow {
        val sessionId = UUID.randomUUID()
        val start = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        return DebugFlow(
            name,
            expectedStatus,
            sessionId,
            codes.mapIndexed { i, code ->
                debugEvent(code, sessionId, start.plusSeconds(i.toLong()))
            })
    }

    // exhaustive: a new event code in posgw-common breaks the build until it is added here
    private fun debugEvent(code: SessionEventCode, sessionId: UUID, at: Instant): SessionEvent<*> =
        when (code) {
            SESSION_INITIALIZED_EVT ->
                SessionInitializedEvent(
                    sessionId,
                    SessionInitializedData(
                        Psp("debug-psp", "debug-broker", "debug-channel"),
                        SessionInitializedData.CreditorInstitution(
                            "00000000000", "debug-office", "debug-workstation"),
                        listOf("debug-terminal"),
                        UUID.randomUUID().toString()),
                    timestamp = at)
            DEBT_POSITION_CREATED_EVT ->
                DeptPositionCreatedEvent(
                    sessionId,
                    DebtPositionCreatedData(
                        listOf(DebtPosition("debug-iupd", "debug-rpt-id", "debug-debtor-hash"))),
                    timestamp = at)
            TRANSACTION_ACTIVATED_EVT ->
                TransactionActivatedEvent(
                    sessionId,
                    TransactionActivatedData(
                        "debug-transaction",
                        listOf(
                            TransactionActivatedData.ActivationInfo(
                                "debug-payment-token", "debug-rpt-id", 1000, "OK", null))),
                    timestamp = at)
            AUTH_REQUESTED_EVT ->
                AuthRequestedEvent(
                    sessionId, AuthRequestedData("debug-terminal", "debug-auth"), timestamp = at)
            AUTH_REQUEST_ERROR_EVT ->
                AuthRequestErrorEvent(
                    sessionId, AuthRequestErrorData("504", "debug auth error"), timestamp = at)
            AUTH_REQUEST_RETRIED_EVT ->
                AuthRequestRetriedEvent(
                    sessionId, AuthRequestRetriedData("504", "debug auth error", 1), timestamp = at)
            AUTH_OUTCOME_RECEIVED_EVT ->
                AuthOutcomeReceivedEvent(
                    sessionId,
                    AuthOutcomeReceivedData(
                        AuthOutcomeReceivedData.AuthDetails(
                            "AUTHORIZED", "000000000000", "debug-auth-code"),
                        InstrumentDetails.CardDetails("VISA", "0000", "000000")),
                    timestamp = at)
            CI_NOTIFIED_EVT -> CiNotifiedEvent(sessionId, timestamp = at)
            CI_NTF_ERROR_EVT ->
                CiNtfErrorEvent(sessionId, CiNtfErrorData("503", "debug ci error"), timestamp = at)
            CI_NTF_RETRIED_EVT ->
                CiNtfRetriedEvent(
                    sessionId, CiNtfRetriedData(1, "503", "debug ci error"), timestamp = at)
            CI_NTF_EXPIRED_EVT -> CiNotifyExpiredEvent(sessionId, timestamp = at)
            ECOMM_NOTIFIED_EVT -> EcommNotifyEvent(sessionId, timestamp = at)
            ECOMM_NTF_ERROR_EVT ->
                EcommNtfErrorEvent(
                    sessionId, EcommNtfErrorData("503", "debug ecommerce error"), timestamp = at)
            ECOMM_NTF_RETRIED_EVT ->
                EcommNtfRetriedEvent(
                    sessionId,
                    EcommNtfRetriedData(1, "503", "debug ecommerce error"),
                    timestamp = at)
            ECOMM_NTF_EXPIRED_EVT -> EcommNtfExpiredEvent(sessionId, timestamp = at)
            // atStatus values match the only flows that use these events
            ERROR_EVT ->
                ErrorEvent(
                    sessionId,
                    ErrorData("AUTH_REQUEST_ERROR", "400", "debug terminal error"),
                    timestamp = at)
            EXPIRED_EVT -> ExpiredEvent(sessionId, ExpiredData("CI_NTF_EXPIRED"), timestamp = at)
            RECONCILED_EVT -> ReconciledEvent(sessionId, timestamp = at)
        }

    // the root cause carries the actual problem (e.g. the mapping error behind "Cannot create
    // PersistentEntity")
    private fun mongoErrorResponse(error: Throwable): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyValue(
                mapOf(
                    "result" to
                        "Mongo debug operation failed: ${NestedExceptionUtils.getMostSpecificCause(error)}"))
}
