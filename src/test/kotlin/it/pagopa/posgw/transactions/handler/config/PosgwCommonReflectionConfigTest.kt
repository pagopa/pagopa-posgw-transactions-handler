package it.pagopa.posgw.transactions.handler.config

import it.pagopa.posgw.common.documents.BaseSessionEvent
import it.pagopa.posgw.common.documents.v1.sessions.SessionView
import it.pagopa.posgw.common.documents.v1.sessions.data.Psp
import it.pagopa.posgw.common.documents.v1.sessions.data.SessionInitializedData
import it.pagopa.posgw.common.documents.v1.sessions.events.SessionInitializedEvent
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates

class PosgwCommonReflectionConfigTest {

    private val hints =
        RuntimeHints().also {
            PosgwCommonDocumentsRuntimeHints().registerHints(it, javaClass.classLoader)
        }

    private fun assertRegistered(type: Class<*>) =
        assertTrue(
            RuntimeHintsPredicates.reflection()
                .onType(type)
                .withMemberCategories(
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS)
                .test(hints),
            "${type.name} not registered")

    @Test
    fun `registers concrete events resolved from _class`() {
        assertRegistered(SessionInitializedEvent::class.java)
    }

    @Test
    fun `registers data and nested classes reachable only through events`() {
        assertRegistered(SessionInitializedData::class.java)
        assertRegistered(SessionInitializedData.CreditorInstitution::class.java)
        assertRegistered(Psp::class.java)
    }

    @Test
    fun `registers the repositories' domain types`() {
        assertRegistered(BaseSessionEvent::class.java)
        assertRegistered(SessionView::class.java)
    }
}
