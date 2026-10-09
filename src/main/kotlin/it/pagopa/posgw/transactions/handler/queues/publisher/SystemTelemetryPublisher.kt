package it.pagopa.posgw.transactions.handler.queues.publisher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SystemTelemetryPublisher(
    @Value($$"${azure.servicebus.topics.system.telemetry.connection-string}")
    override val connectionString: String,
    @Value($$"${azure.servicebus.topics.system.telemetry.topic-name}")
    override val resourceName: String
) : Publisher {
    override val logger: Logger = LoggerFactory.getLogger(SystemTelemetryPublisher::class.java)
    override val client = clientBuilder()
}
