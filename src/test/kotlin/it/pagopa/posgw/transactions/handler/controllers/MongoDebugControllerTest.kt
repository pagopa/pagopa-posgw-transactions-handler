package it.pagopa.posgw.transactions.handler.controllers

import it.pagopa.posgw.common.documents.BaseSessionEvent
import it.pagopa.posgw.common.documents.v1.sessions.SessionView
import it.pagopa.posgw.common.documents.v1.sessions.data.SessionInitializedData
import it.pagopa.posgw.transactions.handler.repositories.SessionsEventStoreRepository
import it.pagopa.posgw.transactions.handler.repositories.SessionsViewRepository
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MongoDebugControllerTest {

    @Suppress("UNCHECKED_CAST")
    private val eventStoreRepository =
        mock(SessionsEventStoreRepository::class.java)
            as SessionsEventStoreRepository<SessionInitializedData>
    private val viewRepository = mock(SessionsViewRepository::class.java)

    private val savedEvents = mutableListOf<BaseSessionEvent<SessionInitializedData>>()
    private val savedViews = mutableListOf<SessionView>()

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        // in-memory behaviour, so the endpoints read back what they wrote
        `when`(eventStoreRepository.save(any<BaseSessionEvent<SessionInitializedData>>()))
            .thenAnswer {
                val event = it.getArgument<BaseSessionEvent<SessionInitializedData>>(0)
                savedEvents.add(event)
                Mono.just(event)
            }
        `when`(eventStoreRepository.findById(anyString())).thenAnswer { invocation ->
            Mono.justOrEmpty(savedEvents.find { it.id == invocation.getArgument(0) })
        }
        // any() returns null, which Kotlin rejects for the non-null sessionId parameter
        `when`(
                eventStoreRepository.findBySessionIdOrderByTimestampAsc(
                    any(UUID::class.java) ?: UUID(0, 0)))
            .thenAnswer { invocation ->
                Flux.fromIterable(savedEvents.filter { it.sessionId == invocation.getArgument(0) })
            }
        `when`(viewRepository.save(any(SessionView::class.java))).thenAnswer {
            val view = it.getArgument<SessionView>(0)
            savedViews.add(view)
            Mono.just(view)
        }
        `when`(viewRepository.findById(anyString())).thenAnswer { invocation ->
            Mono.justOrEmpty(savedViews.find { it.id == invocation.getArgument(0) })
        }

        webTestClient =
            WebTestClient.bindToRouterFunction(
                    MongoDebugController(eventStoreRepository, viewRepository).mongoDebugRouter())
                .build()
    }

    @Test
    fun `POST writes an event and a view and reads them back`() {
        webTestClient
            .post()
            .uri("/debug/mongo")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.eventReadBack")
            .isEqualTo(true)
            .jsonPath("$.sessionId")
            .isEqualTo(savedEvents.single().sessionId.toString())
            .jsonPath("$.events.length()")
            .isEqualTo(1)
            .jsonPath("$.events[0].id")
            .isEqualTo(savedEvents.single().id)
            .jsonPath("$.events[0].type")
            .isEqualTo("SessionInitializedEvent")
            .jsonPath("$.events[0].eventCode")
            .isEqualTo("SESSION_INITIALIZED_EVT")
            .jsonPath("$.view.id")
            .isEqualTo(savedViews.single().id)
            .jsonPath("$.view.status")
            .isEqualTo("INITIALIZED")
            .jsonPath("$.view.events[0].id")
            .isEqualTo(savedEvents.single().id)
    }

    @Test
    fun `POST returns the error when a write fails`() {
        doReturn(Mono.error<Any>(IllegalStateException("mapping failed")))
            .`when`(eventStoreRepository)
            .save(any<BaseSessionEvent<SessionInitializedData>>())

        webTestClient
            .post()
            .uri("/debug/mongo")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody()
            .jsonPath("$.result")
            .isEqualTo(
                "Mongo debug operation failed: java.lang.IllegalStateException: mapping failed")
    }

    @Test
    fun `POST returns the root cause when the mapping fails before the write starts`() {
        doThrow(
                IllegalStateException(
                    "cannot create entity", IllegalArgumentException("ambiguous mapping")))
            .`when`(eventStoreRepository)
            .save(any<BaseSessionEvent<SessionInitializedData>>())

        webTestClient
            .post()
            .uri("/debug/mongo")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody()
            .jsonPath("$.result")
            .isEqualTo(
                "Mongo debug operation failed: java.lang.IllegalArgumentException: ambiguous mapping")
    }

    @Test
    fun `GET reads back a session written by POST`() {
        webTestClient.post().uri("/debug/mongo").exchange().expectStatus().isOk
        val event = savedEvents.single()

        webTestClient
            .get()
            .uri("/debug/mongo/${event.sessionId}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.events[0].id")
            .isEqualTo(event.id)
            .jsonPath("$.view.id")
            .isEqualTo(event.sessionId.toString())
            .jsonPath("$.eventReadBack")
            .doesNotExist()
    }

    @Test
    fun `GET returns no events and an empty view for an unknown session`() {
        webTestClient
            .get()
            .uri("/debug/mongo/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.events.length()")
            .isEqualTo(0)
            .jsonPath("$.view")
            .isEmpty
    }

    @Test
    fun `GET rejects a session id that is not a UUID`() {
        webTestClient
            .get()
            .uri("/debug/mongo/not-a-uuid")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.result")
            .isEqualTo("Invalid session id")
    }

    @Test
    fun `GET returns the error when a read fails`() {
        `when`(viewRepository.findById(anyString()))
            .thenReturn(Mono.error(IllegalStateException("read failed")))

        webTestClient
            .get()
            .uri("/debug/mongo/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody()
            .jsonPath("$.result")
            .isEqualTo("Mongo debug operation failed: java.lang.IllegalStateException: read failed")
    }
}
