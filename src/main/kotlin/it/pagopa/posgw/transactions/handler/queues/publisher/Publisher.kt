package it.pagopa.posgw.transactions.handler.queues.publisher

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient
import org.slf4j.Logger
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.UUID

interface Publisher {
    val connectionString: String
    val resourceName: String
    val logger: Logger
    val client: ServiceBusSenderAsyncClient

    fun clientBuilder(): ServiceBusSenderAsyncClient = ServiceBusClientBuilder()
        .connectionString(connectionString)
        .sender()
        .queueName(resourceName)
        .buildAsyncClient()

    fun publish(message: String, properties: Map<String, Any> = mapOf()): Mono<String> {
        val queueMessage = ServiceBusMessage(message)
            .apply {
                messageId = UUID.randomUUID().toString()
                applicationProperties.putAll(properties)
            }


        return client.sendMessage(queueMessage)
            .map { _ -> queueMessage.messageId }
            .switchIfEmpty { Mono.just(queueMessage.messageId) }
            .doOnNext { logger.info("Message published from ${this.javaClass.name} with id: ${queueMessage.messageId}") }
    }
}