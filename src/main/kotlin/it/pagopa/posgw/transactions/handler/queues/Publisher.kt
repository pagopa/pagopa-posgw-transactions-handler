package it.pagopa.posgw.transactions.handler.queues

import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderClient
import org.slf4j.Logger

abstract class Publisher(
    connectionString: String,
    resourceName: String
) {
    open lateinit var logger: Logger
    open lateinit var client: ServiceBusSenderClient

    abstract fun publish(message: ServiceBusMessage)
}