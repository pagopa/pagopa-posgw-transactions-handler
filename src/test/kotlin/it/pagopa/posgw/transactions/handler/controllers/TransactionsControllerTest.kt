package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.generated.posgw.transactions.handler.model.AuthorizationOutcomeDetailsDto
import it.pagopa.generated.posgw.transactions.handler.model.PaymentRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.UUID

class TransactionsControllerTest {

    private lateinit var controller: TransactionsController
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        controller = TransactionsController()
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun `ciCreatePosPayment direct call throws NotImplementedError`() {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
        val body = Mono.just(PaymentRequestDto())

        assertThrows<NotImplementedError> {
            controller.ciCreatePosPayment(sessionId, correlationId, body, exchange).block()
        }
    }

    @Test
    fun `paymentAuthorizationCallback direct call throws NotImplementedError`() {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
        val body = Mono.just(AuthorizationOutcomeDetailsDto())

        assertThrows<NotImplementedError> {
            controller.paymentAuthorizationCallback(correlationId, sessionId, body, exchange).block()
        }
    }

    @Test
    fun `posGatewayGetPaymentSession direct call throws NotImplementedError`() {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

        assertThrows<NotImplementedError> {
            controller.posGatewayGetPaymentSession(sessionId, correlationId, exchange).block()
        }
    }

    @Test
    fun `GET posGatewayGetPaymentSession endpoint returns 500 when TODO is executed`() {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()

        webTestClient.get()
            .uri("/pos/sessions/$sessionId")
            .header("x-correlation-id", correlationId.toString())
            .exchange()
            .expectStatus().is5xxServerError
    }
}