package it.pagopa.posgw.transactions.handler.queues.subscriber

import com.azure.messaging.servicebus.ServiceBusProcessorClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class EcommerceSyncRetrySubscriber(
    @Value($$"${azure.servicebus.queues.ecommerce.sync-retry.connection-string}")
    override val connectionString: String,
    @Value($$"${azure.servicebus.queues.ecommerce.sync-retry.queue-name}")
    override val resourceName: String
) : Subscriber {

    override val logger: Logger = LoggerFactory.getLogger(EcommerceSyncRetrySubscriber::class.java)
    override val client: ServiceBusProcessorClient = clientBuilder()
}
