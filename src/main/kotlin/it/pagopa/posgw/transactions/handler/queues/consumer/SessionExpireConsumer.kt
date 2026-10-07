package it.pagopa.posgw.transactions.handler.queues.consumer

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import it.pagopa.posgw.transactions.handler.queues.Subscriber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SessionExpireConsumer(
    @Value($$"${azure.servicebus.connection-string}") private val connectionString: String,
    @Value($$"${azure.servicebus.queues.session.expire}") private val queueName: String
) : Subscriber(connectionString, queueName) {

    override var logger: Logger = LoggerFactory.getLogger(SessionExpireConsumer::class.java)
    override var client: ServiceBusProcessorClient = ServiceBusClientBuilder()
        .connectionString(connectionString)
        .processor()
        .queueName(queueName)
        .processMessage(::processMessage)
        .processError(::processError)
        .buildProcessorClient()
}