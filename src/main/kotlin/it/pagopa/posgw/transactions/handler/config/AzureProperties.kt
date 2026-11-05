package it.pagopa.posgw.transactions.handler.config

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AzureProperties {
    @Bean fun azureGlobalProperties(): AzureGlobalProperties = AzureGlobalProperties()
}
