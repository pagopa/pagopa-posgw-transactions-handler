package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.posgw.common.repositories.redis.v1.PaymentSession
import it.pagopa.posgw.common.repositories.redis.v1.PaymentSessionTemplateWrapper
import it.pagopa.posgw.transactions.handler.queues.publisher.*
import java.time.Instant
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.*
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
    val paymentSessionTemplateWrapper: PaymentSessionTemplateWrapper
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
}
