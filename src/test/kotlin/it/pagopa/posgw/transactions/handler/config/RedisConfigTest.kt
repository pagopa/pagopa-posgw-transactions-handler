package it.pagopa.posgw.transactions.handler.config

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.Mockito.mock
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory

class RedisConfigTest {

    private val reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory = mock()

    private val redisConfig = RedisConfig()

    @Test
    fun `should build payment methods RedisTemplateWrapper successfully`() {
        // pre-requisites
        val ttl = 10L
        // test
        val paymentSessionTemplateWrapper =
            redisConfig.paymentMethodsRedisTemplate(
                reactiveRedisConnectionFactory = reactiveRedisConnectionFactory, ttlSeconds = ttl)
        // assertions
        assertEquals(Duration.ofSeconds(ttl), paymentSessionTemplateWrapper.defaultTTL)
        assertNotNull(paymentSessionTemplateWrapper.unwrap())
    }
}
