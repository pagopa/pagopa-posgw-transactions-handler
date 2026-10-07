package it.pagopa.posgw.transactions.handler.queues.subscriber

import com.azure.messaging.servicebus.ServiceBusProcessorClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GecSyncSubscriber(
    @Value($$"${azure.servicebus.connection-string}") override val connectionString: String,
    @Value($$"${azure.servicebus.queues.gec.sync}") override val resourceName: String
) : Subscriber {

    override var logger: Logger = LoggerFactory.getLogger(GecSyncSubscriber::class.java)
    override var client: ServiceBusProcessorClient = clientBuilder()
}