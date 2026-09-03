package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.generated.posgw.transactions.handler.api.TransactionsApi
import it.pagopa.generated.posgw.transactions.handler.model.ActivatePaymentRequestDto
import it.pagopa.generated.posgw.transactions.handler.model.ActivatePaymentResponseDto
import it.pagopa.generated.posgw.transactions.handler.model.AuthorizationCallbackRequestDto
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class TransactionsController : TransactionsApi {

    override fun newTransaction(
        xCorrelationId: UUID,
        activatePaymentRequestDto: Mono<ActivatePaymentRequestDto>,
        exchange: ServerWebExchange,
    ): Mono<ResponseEntity<ActivatePaymentResponseDto>> =
        Mono.just(ResponseEntity.ok(ActivatePaymentResponseDto()))

    override fun notifyAuthorizationOutcome(
        transactionId: UUID,
        xCorrelationId: UUID,
        authorizationCallbackRequestDto: Mono<AuthorizationCallbackRequestDto>,
        exchange: ServerWebExchange,
    ): Mono<ResponseEntity<Void>> = Mono.just(ResponseEntity.noContent().build())
}
