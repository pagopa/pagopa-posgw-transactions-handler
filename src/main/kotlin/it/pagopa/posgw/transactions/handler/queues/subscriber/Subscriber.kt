package it.pagopa.posgw.transactions.handler.queues.subscriber

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusErrorContext
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.function.Consumer
import org.slf4j.Logger

interface Subscriber {

    val connectionString: String
    val resourceName: String
    val logger: Logger
    val client: ServiceBusProcessorClient

    fun clientBuilder(): ServiceBusProcessorClient =
        ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(resourceName)
            .processMessage { m -> getMessageProcessor().onMessage(m) }
            .processError { e -> getErrorProcessor().accept(e) }
            .buildProcessorClient()

    fun getMessageProcessor(): ServiceBusRecordMessageListener = { ctx ->
        logger.info(
            """
            >>>>>>>>RECEIVED MESSAGE FROM $resourceName<<<<<<<<
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
            >>>>>>>>END MESSAGE FROM $resourceName<<<<<<<<
        """
                .trimIndent())
    }

    fun getErrorProcessor(): Consumer<ServiceBusErrorContext> = Consumer { ctx ->
        logger.info(
            """
            >>>>>>>>RECEIVED ERROR<<<<<<<<
            ENTITY_PATH: ${ctx.entityPath}
            ERROR_SOURCE: ${ctx.errorSource}
            EXCEPTION: ${ctx.exception}
            FULLY_QUALIFIED_NAMESPACE: ${ctx.fullyQualifiedNamespace}
            >>>>>>>>END RECEIVED ERROR<<<<<<<<
        """
                .trimIndent())
    }

    @PostConstruct
    fun start() {
        logger.info(
            "Started consumer ${this.javaClass.name} with topic ${client.topicName} / queue ${client.queueName}")
        client.start()
    }

    @PreDestroy
    fun stop() {
        logger.info("Stopped consumer ${this.javaClass.name}")
        client.stop()
    }
}
