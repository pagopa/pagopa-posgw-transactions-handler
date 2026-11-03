package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.posgw.common.documents.BaseSessionEvent
import it.pagopa.posgw.common.documents.v1.sessions.SessionView
import it.pagopa.posgw.common.documents.v1.sessions.data.Psp
import it.pagopa.posgw.common.documents.v1.sessions.data.SessionInitializedData
import it.pagopa.posgw.common.documents.v1.sessions.events.SessionInitializedEvent
import it.pagopa.posgw.transactions.handler.repositories.SessionsEventStoreRepository
import it.pagopa.posgw.transactions.handler.repositories.SessionsViewRepository
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

// Temporary endpoints to verify MongoDB read/write on each environment: remove once sessions are
// persisted by the real flow
@RestController
class MongoDebugController(
    @Autowired val eventStoreRepository: SessionsEventStoreRepository<SessionInitializedData>,
    @Autowired val viewRepository: SessionsViewRepository
) {

    @Bean
    fun mongoDebugRouter(): RouterFunction<*> = router {
        POST("/debug/mongo").invoke(writeAndRead)
        GET("/debug/mongo/{sessionId}").invoke(read)
    }

    val writeAndRead: (ServerRequest) -> Mono<ServerResponse> = {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        val event =
            SessionInitializedEvent(
                sessionId,
                SessionInitializedData(
                    Psp("debug-psp", "debug-broker", "debug-channel"),
                    SessionInitializedData.CreditorInstitution(
                        "00000000000", "debug-office", "debug-workstation"),
                    listOf("debug-terminal"),
                    correlationId))
        val now = Instant.now()
        val view =
            SessionView(
                id = sessionId.toString(),
                correlationId = correlationId,
                status = "INITIALIZED",
                createdAt = now,
                updatedAt = now,
                events = listOf(event))

        // deferred: the template maps the entity when save is called, so mapping errors are thrown
        // before any Mono exists
        Mono.defer { eventStoreRepository.save(event) }
            .flatMap { viewRepository.save(view) }
            .flatMap {
                eventStoreRepository.findById(event.id).map { it == event }.defaultIfEmpty(false)
            }
            .flatMap { readBack ->
                readSession(sessionId).map { it + ("eventReadBack" to readBack) }
            }
            .flatMap { ServerResponse.ok().bodyValue(it) }
            .onErrorResume(::errorResponse)
    }

    val read: (ServerRequest) -> Mono<ServerResponse> = { req ->
        val sessionId = runCatching { UUID.fromString(req.pathVariable("sessionId")) }.getOrNull()
        if (sessionId == null) {
            ServerResponse.badRequest().bodyValue(mapOf("result" to "Invalid session id"))
        } else {
            Mono.defer { readSession(sessionId) }
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .onErrorResume(::errorResponse)
        }
    }

    private fun readSession(sessionId: UUID): Mono<Map<String, Any?>> =
        eventStoreRepository
            .findBySessionIdOrderByTimestampAsc(sessionId)
            .map { it.summary() }
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
                    .defaultIfEmpty(mapOf()))
            .map { mapOf("sessionId" to sessionId.toString(), "events" to it.t1, "view" to it.t2) }

    // the concrete type shows whether the polymorphic read restored the right event class
    private fun BaseSessionEvent<*>.summary(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "type" to javaClass.simpleName,
            "eventCode" to eventCode,
            "timestamp" to timestamp.toString())

    // the root cause carries the actual problem (e.g. the mapping error behind "Cannot create
    // PersistentEntity")
    private fun errorResponse(error: Throwable): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyValue(
                mapOf(
                    "result" to
                        "Mongo debug operation failed: ${NestedExceptionUtils.getMostSpecificCause(error)}"))
}
