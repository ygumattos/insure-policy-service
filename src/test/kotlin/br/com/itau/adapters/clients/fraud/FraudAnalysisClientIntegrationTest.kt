package br.com.itau.adapters.clients.fraud

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [RestTemplateConfig::class])
class FraudAnalysisClientIntegrationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate
    private lateinit var fraudAnalysisClient: FraudAnalysisClient

    @BeforeEach
    fun setUp() {
        fraudAnalysisClient = FraudAnalysisClient(restTemplate, "http://localhost:8089")
    }

    @Test
    fun `should return REGULAR classification for customer ID starting with 21`() {
        val customerId = "21000000-0000-0000-0000-000000000000"

        val result = fraudAnalysisClient.analyzeFraud(customerId)

        assertEquals(CustomerClassification.REGULAR, result.classification)
    }

    @Test
    fun `should return HIGH_RISK classification for customer ID starting with 22`() {
        val customerId = "22000000-0000-0000-0000-000000000000"

        val result = fraudAnalysisClient.analyzeFraud(customerId)

        assertEquals(CustomerClassification.HIGH_RISK, result.classification)
    }

    @Test
    fun `should return PREFERENTIAL classification for customer ID starting with 23`() {
        val customerId = "23000000-0000-0000-0000-000000000000"

        val result = fraudAnalysisClient.analyzeFraud(customerId)

        assertEquals(CustomerClassification.PREFERENTIAL, result.classification)
    }

    @Test
    fun `should return NO_INFORMATION classification for customer ID starting with 24`() {
        val customerId = "24000000-0000-0000-0000-000000000000"

        val result = fraudAnalysisClient.analyzeFraud(customerId)

        assertEquals(CustomerClassification.NO_INFORMATION, result.classification)
    }

    @Test
    fun `should throw FraudAnalysisException for customer ID starting with 44 (Not Found)`() {
        val customerId = "44000000-0000-0000-0000-000000000000"

        val exception = assertThrows<FraudAnalysisException> {
            fraudAnalysisClient.analyzeFraud(customerId)
        }

        assertEquals("Customer not found: $customerId", exception.message)
    }

    @Test
    fun `should throw FraudAnalysisException for customer ID starting with 55 (Server Error)`() {
        val customerId = "55000000-0000-0000-0000-000000000000"

        val exception = assertThrows<FraudAnalysisException> {
            fraudAnalysisClient.analyzeFraud(customerId)
        }

        assertEquals("Fraud analysis service unavailable", exception.message)
    }

    @Test
    fun `should handle multiple valid customer classifications dynamically`() {
        val testCases = mapOf(
            "21000000-0000-0000-0000-000000000000" to CustomerClassification.REGULAR,
            "22000000-0000-0000-0000-000000000000" to CustomerClassification.HIGH_RISK,
            "23000000-0000-0000-0000-000000000000" to CustomerClassification.PREFERENTIAL,
            "24000000-0000-0000-0000-000000000000" to CustomerClassification.NO_INFORMATION
        )

        testCases.forEach { (customerId, expected) ->
            val result = fraudAnalysisClient.analyzeFraud(customerId)
            assertEquals(expected, result.classification)
        }
    }
}
