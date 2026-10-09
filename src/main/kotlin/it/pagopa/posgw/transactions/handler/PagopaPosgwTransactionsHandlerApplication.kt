package it.pagopa.posgw.transactions.handler

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication class PagopaPosgwTransactionsHandlerApplication

@Bean
fun azureGlobalProperties(): AzureGlobalProperties {
    return AzureGlobalProperties()
}

fun main(args: Array<String>) {
    runApplication<PagopaPosgwTransactionsHandlerApplication>(*args)
}
