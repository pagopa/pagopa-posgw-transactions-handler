package it.pagopa.posgw.transactions.handler.queues.publisher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SessionExpirePublisher(
    @Value($$"${azure.servicebus.queues.session.expire.connection-string}") override val connectionString: String,
    @Value($$"${azure.servicebus.queues.session.expire.queue-name}") override val resourceName: String
) : Publisher {
    override val logger: Logger = LoggerFactory.getLogger(SessionExpirePublisher::class.java)
    override val client = clientBuilder()
}