package br.com.itau.adapters.clients.fraud

import br.com.itau.adapters.clients.fraud.dtos.FraudAnalysisResponse
import br.com.itau.domain.enums.CustomerClassification
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

class FraudAnalysisClientTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var fraudAnalysisClient: FraudAnalysisClient

    private val baseUrl = "http://localhost:8089"
    private val customerId = "21000000-0000-0000-0000-000000000000"

    @BeforeEach
    fun setUp() {
        restTemplate = mockk()
        fraudAnalysisClient = FraudAnalysisClient(restTemplate, baseUrl)
    }

    @Test
    fun `should return fraud analysis result when API responds successfully`() {
        // Given
        val expectedResponse = FraudAnalysisResponse(
            classification = "REGULAR"
        )

        val responseEntity = ResponseEntity.ok(expectedResponse)

        every {
            restTemplate.postForEntity(
                "$baseUrl/api/v1/fraud/analysis",
                any<FraudAnalysisRequest>(),
                FraudAnalysisResponse::class.java
            )
        } returns responseEntity

        // When
        val result = fraudAnalysisClient.analyzeFraud(customerId)

        // Then
        assertEquals(CustomerClassification.REGULAR, result.classification)
    }

    @Test
    fun `should throw FraudAnalysisException when customer not found`() {
        // Given
        every {
            restTemplate.postForEntity(
                any<String>(),
                any<FraudAnalysisRequest>(),
                any<Class<FraudAnalysisResponse>>()
            )
        } throws HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "Not Found",
            null,
            null,
            null
        )

        // When & Then
        val exception = assertThrows<FraudAnalysisException> {
            fraudAnalysisClient.analyzeFraud(customerId)
        }

        assertEquals("Customer not found: $customerId", exception.message)
    }

    @Test
    fun `should throw FraudAnalysisException when server error occurs`() {
        // Given
        every {
            restTemplate.postForEntity(
                any<String>(),
                any<FraudAnalysisRequest>(),
                any<Class<FraudAnalysisResponse>>()
            )
        } throws HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            null,
            null,
            null
        )

        // When & Then
        val exception = assertThrows<FraudAnalysisException> {
            fraudAnalysisClient.analyzeFraud(customerId)
        }

        assertEquals("Fraud analysis service unavailable", exception.message)
    }

    @Test
    fun `should throw FraudAnalysisException when unexpected error occurs`() {
        // Given
        every {
            restTemplate.postForEntity(
                any<String>(),
                any<FraudAnalysisRequest>(),
                any<Class<FraudAnalysisResponse>>()
            )
        } throws RuntimeException("Connection timeout")

        // When & Then
        val exception = assertThrows<FraudAnalysisException> {
            fraudAnalysisClient.analyzeFraud(customerId)
        }

        assertEquals("Failed to analyze fraud", exception.message)
    }

    @Test
    fun `should handle different customer classifications correctly`() {
        // Given
        val testCases = listOf(
            "REGULAR" to CustomerClassification.REGULAR,
            "HIGH_RISK" to CustomerClassification.HIGH_RISK,
            "PREFERENTIAL" to CustomerClassification.PREFERENTIAL,
            "NO_INFORMATION" to CustomerClassification.NO_INFORMATION
        )

        testCases.forEach { (classification, expected) ->
            // Setup mock para cada caso
            every {
                restTemplate.postForEntity(
                    any<String>(),
                    any<FraudAnalysisRequest>(),
                    FraudAnalysisResponse::class.java
                )
            } returns ResponseEntity.ok(
                FraudAnalysisResponse(
                    classification = classification
                )
            )

            // When
            val result = fraudAnalysisClient.analyzeFraud(customerId)

            // Then
            assertEquals(expected, result.classification)

            // Reset mock para próximo teste
            clearMocks(restTemplate)
        }
    }

    private data class FraudAnalysisRequest(
        val customerId: String
    )
}