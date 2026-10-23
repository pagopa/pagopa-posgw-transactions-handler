package it.pagopa.posgw.transactions.handler.config

import it.pagopa.posgw.common.repositories.redis.v1.PaymentSession
import it.pagopa.posgw.common.repositories.redis.v1.PaymentSessionTemplateWrapper
import java.time.Duration
import kotlin.reflect.KClass
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.module.kotlin.jacksonObjectMapper

@Configuration
class RedisConfig {

    @Bean
    fun paymentSessionRedisTemplate(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
        @Value($$"${payment-session.cache.ttlSeconds}") ttlSeconds: Long,
    ): PaymentSessionTemplateWrapper {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = buildJacksonRedisSerializer(PaymentSession::class)

        val serializationContext =
            RedisSerializationContext.newSerializationContext<String, PaymentSession>(keySerializer)
                .value(valueSerializer)
                .build()

        val reactiveRedisTemplate =
            ReactiveRedisTemplate(reactiveRedisConnectionFactory, serializationContext)

        return PaymentSessionTemplateWrapper(
            reactiveRedisTemplate = reactiveRedisTemplate, ttl = Duration.ofSeconds(ttlSeconds))
    }

    private fun <T : Any> buildJacksonRedisSerializer(
        clazz: KClass<T>
    ): JacksonJsonRedisSerializer<T> {
        val jacksonObjectMapper = jacksonObjectMapper()
        return JacksonJsonRedisSerializer(jacksonObjectMapper, clazz.java)
    }
}
