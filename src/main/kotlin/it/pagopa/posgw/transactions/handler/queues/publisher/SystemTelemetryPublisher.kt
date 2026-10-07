package it.pagopa.posgw.transactions.handler.queues.publisher

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderClient
import it.pagopa.posgw.transactions.handler.queues.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SystemTelemetryPublisher(
    @Value($$"${azure.servicebus.connection-string}") private val connectionString: String,
    @Value($$"${azure.servicebus.topics.system.telemetry}") private val topicName: String
) : Publisher(connectionString, topicName) {
    override var logger: Logger = LoggerFactory.getLogger(SystemTelemetryPublisher::class.java)

    override var client: ServiceBusSenderClient = ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .topicName(topicName)
            .buildClient()

    override fun publish(message: ServiceBusMessage) {
        client.sendMessage(message)
        logger.info("Message published from ${this.javaClass.name} with id: ${message.messageId}")
    }
}