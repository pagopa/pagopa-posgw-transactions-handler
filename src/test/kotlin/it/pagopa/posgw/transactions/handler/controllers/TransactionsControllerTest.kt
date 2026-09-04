package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.generated.posgw.transactions.handler.model.*
import java.time.OffsetDateTime
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.web.reactive.server.WebTestClient

class TransactionsControllerTest {

    private lateinit var controller: TransactionsController
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        controller = TransactionsController()
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun `ciCreatePosPayment direct call throws NotImplementedError`() = runTest {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val body = PaymentRequestDto(terminalId = "12345678", debtPositions = emptyList())

        assertThrows<NotImplementedError> {
            controller.ciCreatePosPayment(sessionId, correlationId, body)
        }
    }

    @Test
    fun `paymentAuthorizationCallback direct call throws NotImplementedError`() = runTest {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val body =
            AuthorizationOutcomeDetailsDto(
                outcome = AuthorizationOutcomeDto.DECLINED,
                terminalId = "12345678",
                timestamp = OffsetDateTime.now(),
                amount = 1000L,
                details = AuthorizationCallbackDetailsKoDto("error"))

        assertThrows<NotImplementedError> {
            controller.paymentAuthorizationCallback(correlationId, sessionId, body)
        }
    }

    @Test
    fun `posGatewayGetPaymentSession direct call throws NotImplementedError`() = runTest {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()

        assertThrows<NotImplementedError> {
            controller.posGatewayGetPaymentSession(sessionId, correlationId)
        }
    }

    @Test
    fun `GET posGatewayGetPaymentSession endpoint returns 500 when TODO is executed`() {
        val sessionId = UUID.randomUUID()
        val correlationId = UUID.randomUUID()

        webTestClient
            .get()
            .uri("/pos/sessions/$sessionId")
            .header("x-correlation-id", correlationId.toString())
            .exchange()
            .expectStatus()
            .is5xxServerError
    }
}
