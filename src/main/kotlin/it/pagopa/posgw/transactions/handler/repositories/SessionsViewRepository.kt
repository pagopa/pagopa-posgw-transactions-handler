package it.pagopa.posgw.transactions.handler.repositories

import it.pagopa.posgw.common.documents.v1.sessions.SessionView
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository interface SessionsViewRepository : ReactiveCrudRepository<SessionView, String>
