package it.pagopa.posgw.transactions.handler.queues.subscriber

import com.azure.messaging.servicebus.ServiceBusProcessorClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SessionPollingSubscriber(
    @Value($$"${azure.servicebus.queues.session.poll.connection-string}") override val connectionString: String,
    @Value($$"${azure.servicebus.queues.session.poll.queue-name}") override val resourceName: String
) : Subscriber {

    override var logger: Logger = LoggerFactory.getLogger(SessionPollingSubscriber::class.java)
    override var client: ServiceBusProcessorClient = clientBuilder()
}