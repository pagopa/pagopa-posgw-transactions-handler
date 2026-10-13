package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.posgw.transactions.handler.queues.publisher.EcommerceSyncPublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.EcommerceSyncRetryPublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.GecSyncPublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.GecSyncRetryPublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.SessionExpirePublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.SessionPollPublisher
import it.pagopa.posgw.transactions.handler.queues.publisher.SystemTelemetryPublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@RestController
class PublisherController(
    @Autowired val ecommerceSyncPublisher: EcommerceSyncPublisher,
    @Autowired val ecommerceSyncRetryPublisher: EcommerceSyncRetryPublisher,
    @Autowired val gecSyncPublisher: GecSyncPublisher,
    @Autowired val gecSyncRetryPublisher: GecSyncRetryPublisher,
    @Autowired val sessionExpirePublisher: SessionExpirePublisher,
    @Autowired val sessionPollPublisher: SessionPollPublisher,
    @Autowired val systemTelemetryPublisher: SystemTelemetryPublisher
) {

    @Bean
    fun router(): RouterFunction<*> = router {
        (POST("/publish") and accept(APPLICATION_JSON)).invoke(publishMessage)
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
}
