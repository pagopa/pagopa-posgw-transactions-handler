package it.pagopa.posgw.transactions.handler.queues.publisher

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
@TestPropertySource(locations = ["classpath:application.test.properties"])
class SessionPollPublisherTest {

    private var sessionPollPublisher: SessionPollPublisher = Mockito.mock()

    @BeforeEach
    fun mock() {
        Mockito
            .`when`(sessionPollPublisher.logger)
            .thenReturn(Mockito.mock())
    }

    @Test
    fun `publish should return messageId UUID on success`()  {
        val clientMock: ServiceBusSenderAsyncClient = Mockito.mock()

        Mockito
            .`when`(sessionPollPublisher.client)
            .thenReturn(clientMock)

        Mockito
            .`when`(sessionPollPublisher.publish(Mockito.anyString(), Mockito.anyMap()))
            .thenCallRealMethod()

        Mockito
            .`when`(clientMock.sendMessage(Mockito.any()))
            .thenReturn(Mono.empty())

        StepVerifier.create(sessionPollPublisher.publish("testMessageBody"))
            .assertNext { s ->
                assertDoesNotThrow { UUID.fromString(s) }
            }
            .verifyComplete()

    }

    @Test
    fun `publish should throw on failure`()  {
        val clientMock: ServiceBusSenderAsyncClient = Mockito.mock()

        Mockito
            .`when`(sessionPollPublisher.client)
            .thenReturn(clientMock)

        Mockito
            .`when`(sessionPollPublisher.publish(Mockito.anyString(), Mockito.anyMap()))
            .thenCallRealMethod()

        Mockito
            .`when`(clientMock.sendMessage(Mockito.any()))
            .thenReturn(Mono.error(RuntimeException()))

        StepVerifier.create(sessionPollPublisher.publish("testMessageBody"))
            .verifyError(RuntimeException::class.java)

    }

}
