package it.pagopa.posgw.transactions.handler

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations = ["classpath:application.test.properties"])
class PagopaPosgwTransactionsHandlerApplicationTests {

    @Test fun contextLoads() {}
}
