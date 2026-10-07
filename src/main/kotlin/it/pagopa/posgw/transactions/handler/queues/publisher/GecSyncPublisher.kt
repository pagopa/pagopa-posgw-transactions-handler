package it.pagopa.posgw.transactions.handler.queues.publisher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GecSyncPublisher(
    @Value($$"${azure.servicebus.connection-string}") override val connectionString: String,
    @Value($$"${azure.servicebus.queues.gec.sync}") override val resourceName: String
) : Publisher {
    override val logger: Logger = LoggerFactory.getLogger(GecSyncPublisher::class.java)
    override val client = clientBuilder()
}