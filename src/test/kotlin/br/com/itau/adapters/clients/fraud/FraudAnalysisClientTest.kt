//package br.com.itau.adapters.clients.fraud
//
//import br.com.itau.domain.enums.CustomerClassification
//import br.com.itau.domain.valueobjects.PolicyRequestId
//import com.github.tomakehurst.wiremock.client.WireMock.*
//import com.github.tomakehurst.wiremock.junit5.WireMockTest
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.TestPropertySource
//import java.util.UUID
//
//@SpringBootTest
//@WireMockTest(httpPort = 8089)
//@TestPropertySource()
//class FraudAnalysisClientTest {
//
//    @Autowired
//    private lateinit var fraudAnalysisClient: FraudAnalysisClient
//
//    @Test
//    fun `should return fraud analysis result when API responds successfully`() {
//        // Given
//        val solicitationId = PolicyRequestId.generate()
//        val customerId = UUID.randomUUID().toString()
//
//        val expectedResponse = """
//            {
//                "solicitationId": "${solicitationId.value}",
//                "customerId": "$customerId",
//                "analysisDate": "2023-10-01T14:00:00Z",
//                "classification": "REGULAR",
//                "occurrences": ["No previous occurrences"]
//            }
//        """.trimIndent()
//
//        stubFor(post(urlEqualTo("/analyze"))
//            .willReturn(aResponse()
//                .withStatus(200)
//                .withHeader("Content-Type", "application/json")
//                .withBody(expectedResponse)))
//
//        // When
//        val result = fraudAnalysisClient.analyzeFraud(solicitationId, customerId)
//
//        // Then
//        assert(result.classification == CustomerClassification.REGULAR)
//
//        verify(postRequestedFor(urlEqualTo("/analyze"))
//            .withRequestBody(equalToJson("""
//                {
//                    "solicitationId": "${solicitationId.value}",
//                    "customerId": "$customerId"
//                }
//            """.trimIndent())))
//    }
//}