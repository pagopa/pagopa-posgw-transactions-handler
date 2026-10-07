package it.pagopa.posgw.transactions.handler.queues.consumer

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import it.pagopa.posgw.transactions.handler.queues.Subscriber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GecSyncConsumer(
    @Value($$"${azure.servicebus.connection-string}") private val connectionString: String,
    @Value($$"${azure.servicebus.queues.gec.sync}") private val queueName: String
) : Subscriber(connectionString, queueName) {

    override var logger: Logger = LoggerFactory.getLogger(GecSyncConsumer::class.java)
    override var client: ServiceBusProcessorClient = ServiceBusClientBuilder()
        .connectionString(connectionString)
        .processor()
        .queueName(queueName)
        .processMessage(::processMessage)
        .processError(::processError)
        .buildProcessorClient()
}