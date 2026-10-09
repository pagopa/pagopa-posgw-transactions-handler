package it.pagopa.posgw.transactions.handler.queues.publisher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GecSyncRetryPublisher(
    @Value($$"${azure.servicebus.queues.gec.sync-retry.connection-string}")
    override val connectionString: String,
    @Value($$"${azure.servicebus.queues.gec.sync-retry.queue-name}")
    override val resourceName: String
) : Publisher {
    override val logger: Logger = LoggerFactory.getLogger(GecSyncRetryPublisher::class.java)
    override val client = clientBuilder()
}
