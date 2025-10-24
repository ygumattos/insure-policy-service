package br.com.itau.adapters.clients.fraud

import br.com.itau.adapters.clients.fraud.dtos.FraudAnalysisResponse
import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.outputs.FraudAnalysis
import br.com.itau.domain.entities.FraudAnalysisResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@Component
class FraudAnalysisClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${app.fraud-analysis.base-url}") private val baseUrl: String
) : FraudAnalysis, Logging {
    
    override fun analyzeFraud(customerId: String): FraudAnalysisResult {
        val request = FraudAnalysisRequest(customerId)

        log.info("Calling fraud analysis API for customer: {}", customerId)

        try {
            val response = restTemplate.postForEntity(
                "$baseUrl/api/v1/fraud/analysis",
                request,
                FraudAnalysisResponse::class.java
            )

            return response.body!!.toDomain()

        } catch (e: HttpClientErrorException.NotFound) {
            log.warn("Customer not found in fraud system: {}", customerId)
            throw FraudAnalysisException("Customer not found: $customerId", e)
        } catch (e: HttpServerErrorException) {
            log.error("Fraud analysis API error for customer: {}", customerId, e)
            throw FraudAnalysisException("Fraud analysis service unavailable", e)
        } catch (e: Exception) {
            log.error("Unexpected error calling fraud analysis API", e)
            throw FraudAnalysisException("Failed to analyze fraud", e)
        }
    }
}

// Exceção específica do domínio para fraud analysis
class FraudAnalysisException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// DTOs internos
private data class FraudAnalysisRequest(
    val customerId: String
)

