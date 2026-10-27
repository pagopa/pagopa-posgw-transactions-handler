package it.pagopa.posgw.transactions.handler.repositories

import it.pagopa.posgw.common.documents.BaseSessionEvent
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SessionsEventStoreRepository<T> : ReactiveCrudRepository<BaseSessionEvent<T>, String> {
    fun findBySessionIdOrderByTimestampAsc(sessionId: UUID): Flux<BaseSessionEvent<T>>
}
