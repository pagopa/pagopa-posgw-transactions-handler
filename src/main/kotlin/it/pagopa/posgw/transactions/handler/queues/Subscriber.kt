package it.pagopa.posgw.transactions.handler.queues

import com.azure.messaging.servicebus.ServiceBusErrorContext
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener
import org.slf4j.Logger

abstract class Subscriber(
    connectionString: String,
    resourceName: String
) {
    open lateinit var logger: Logger
    open lateinit var client: ServiceBusProcessorClient

    open fun processMessage(message: ServiceBusReceivedMessageContext): ServiceBusRecordMessageListener = { ctx ->
        logger.info(
        """
            >>>>>>>>RECEIVED MESSAGE<<<<<<<<
            PROPERTIES: ${ctx.message.applicationProperties}
            MESSAGE_ID: ${ctx.message.messageId}
            BODY: ${ctx.message.body.toBytes().toString(Charsets.UTF_8)}
            STATE: ${ctx.message.state}
            CORRELATION_ID: ${ctx.message.correlationId}
            deliveryCount: ${ctx.message.deliveryCount}
            partitionKey: ${ctx.message.partitionKey}
            sessionId: ${ctx.message.sessionId}
            replyTo: ${ctx.message.replyTo}
            replyToSessionId: ${ctx.message.replyToSessionId}
            enqueuedSequenceNumber: ${ctx.message.enqueuedSequenceNumber}
            timeToLive: ${ctx.message.timeToLive}
            enqueuedTime: ${ctx.message.enqueuedTime}
            deadLetterReason: ${ctx.message.deadLetterReason}
            >>>>>>>>END RECEIVED MESSAGE<<<<<<<<
        """.trimIndent()
        )
    }

    open fun processError(message: ServiceBusErrorContext): ServiceBusRecordMessageListener = { ctx ->
        logger.info(
            """
            >>>>>>>>RECEIVED ERROR<<<<<<<<
            PROPERTIES: ${ctx.message.applicationProperties}
            MESSAGE_ID: ${ctx.message.messageId}
            BODY: ${ctx.message.body.toBytes().toString(Charsets.UTF_8)}
            STATE: ${ctx.message.state}
            CORRELATION_ID: ${ctx.message.correlationId}
            deliveryCount: ${ctx.message.deliveryCount}
            partitionKey: ${ctx.message.partitionKey}
            sessionId: ${ctx.message.sessionId}
            replyTo: ${ctx.message.replyTo}
            replyToSessionId: ${ctx.message.replyToSessionId}
            enqueuedSequenceNumber: ${ctx.message.enqueuedSequenceNumber}
            timeToLive: ${ctx.message.timeToLive}
            enqueuedTime: ${ctx.message.enqueuedTime}
            deadLetterReason: ${ctx.message.deadLetterReason}
            >>>>>>>>END RECEIVED ERROR<<<<<<<<
        """.trimIndent()
        )
    }
}