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
class SessionExpirePublisher(
    @Value($$"${azure.servicebus.connection-string}") private val connectionString: String,
    @Value($$"${azure.servicebus.queues.session.expire}") private val queueName: String
) : Publisher(connectionString, queueName) {
    override var logger: Logger = LoggerFactory.getLogger(SessionExpirePublisher::class.java)

    override var client: ServiceBusSenderClient = ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient()

    override fun publish(message: ServiceBusMessage) {
        client.sendMessage(message)
        logger.info("Message published from ${this.javaClass.name} with id: ${message.messageId}")
    }
}