package it.pagopa.posgw.transactions.handler.configs

import com.azure.core.amqp.models.AmqpMessageBody
import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderClient
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceBusProcessorClientConfiguration(
    @Autowired private val client: ServiceBusSenderClient
) {

    @Bean
    fun processMessage(): ServiceBusRecordMessageListener = { ctx ->
        println(">>>>>>>>RECEIVED MESSAGE<<<<<<<<")
        println("PROPERTIES: " + ctx.message.applicationProperties)
        println("MESSAGE_ID: " + ctx.message.messageId)
        println("BODY: " + ctx.message.body.toBytes().toString(Charsets.UTF_8))
        println("STATE: " + ctx.message.state)
        println("CORRELATION_ID: " + ctx.message.correlationId)
        println("deliveryCount: " + ctx.message.deliveryCount)
        println("partitionKey: " + ctx.message.partitionKey)
        println("sessionId: " + ctx.message.sessionId)
        println("replyTo: " + ctx.message.replyTo)
        println("replyToSessionId: " + ctx.message.replyToSessionId)
        println("enqueuedSequenceNumber: " + ctx.message.enqueuedSequenceNumber)
        println("timeToLive: " + ctx.message.timeToLive)
        println("enqueuedTime: " + ctx.message.enqueuedTime)
        println("deadLetterReason: " + ctx.message.deadLetterReason)
        println(">>>>>>>>END RECEIVED MESSAGE<<<<<<<<")
    }

    @Bean
    fun processError(): ServiceBusErrorHandler = { ctx ->
        println(ctx)
    }

    @PostConstruct
    fun afterBuild() {
        val message =
            ServiceBusMessage(AmqpMessageBody.fromData("test".toByteArray(Charsets.UTF_8)))
        message.correlationId = "correlationId"
        message.sessionId = "sessionId"
        message.applicationProperties["test"] = "test"
        message.applicationProperties["test2"] = "test2"
        message.applicationProperties["test3"] = "test3"
        client.sendMessage(message)
    }
}
