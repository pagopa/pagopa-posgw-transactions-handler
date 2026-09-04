package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.generated.posgw.transactions.handler.api.PosApi
import it.pagopa.generated.posgw.transactions.handler.model.AuthorizationOutcomeDetailsDto
import it.pagopa.generated.posgw.transactions.handler.model.PaymentRequestDto
import it.pagopa.generated.posgw.transactions.handler.model.PaymentResponseDto
import it.pagopa.generated.posgw.transactions.handler.model.SessionStatusResponseDto
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionsController : PosApi {

    override suspend fun ciCreatePosPayment(
        sessionId: UUID,
        xCorrelationId: UUID,
        paymentRequestDto: PaymentRequestDto
    ): ResponseEntity<PaymentResponseDto> {
        TODO("Not yet implemented")
    }

    override suspend fun paymentAuthorizationCallback(
        xCorrelationId: UUID,
        sessionId: UUID,
        authorizationOutcomeDetailsDto: AuthorizationOutcomeDetailsDto
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun posGatewayGetPaymentSession(
        sessionId: UUID,
        xCorrelationId: UUID
    ): ResponseEntity<SessionStatusResponseDto> {
        TODO("Not yet implemented")
    }
}
