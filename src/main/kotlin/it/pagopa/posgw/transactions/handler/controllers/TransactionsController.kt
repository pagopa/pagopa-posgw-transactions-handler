package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.generated.posgw.transactions.handler.api.PosApi
import it.pagopa.generated.posgw.transactions.handler.model.AuthorizationOutcomeDetailsDto
import it.pagopa.generated.posgw.transactions.handler.model.PaymentRequestDto
import it.pagopa.generated.posgw.transactions.handler.model.PaymentResponseDto
import it.pagopa.generated.posgw.transactions.handler.model.SessionStatusResponseDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class TransactionsController : PosApi {

    override fun ciCreatePosPayment(
        sessionId: UUID,
        xCorrelationId: @NotNull UUID,
        paymentRequestDto: @Valid Mono<PaymentRequestDto>,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<PaymentResponseDto>> {
        TODO("Not yet implemented")
    }

    override fun paymentAuthorizationCallback(
        xCorrelationId: @NotNull UUID,
        sessionId: UUID,
        authorizationOutcomeDetailsDto: @Valid Mono<AuthorizationOutcomeDetailsDto>,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Void>> {
        TODO("Not yet implemented")
    }

    override fun posGatewayGetPaymentSession(
        sessionId: UUID,
        xCorrelationId: @NotNull UUID,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<SessionStatusResponseDto>> {
        TODO("Not yet implemented")
    }
}
