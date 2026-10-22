package it.pagopa.posgw.transactions.handler

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration
import com.azure.spring.cloud.autoconfigure.implementation.data.redis.AzureLettucePasswordlessAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude =
        [
            AzureTokenCredentialAutoConfiguration::class,
            AzureLettucePasswordlessAutoConfiguration::class])
class PagopaPosgwTransactionsHandlerApplication

fun main(args: Array<String>) {
    runApplication<PagopaPosgwTransactionsHandlerApplication>(*args)
}
