package br.com.itau.adapters.clients.fraud.dtos

import br.com.itau.domain.entities.FraudAnalysisResult
import br.com.itau.domain.enums.CustomerClassification
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FraudAnalysisResponse(
    val classification: String
) {
    fun toDomain(): FraudAnalysisResult = FraudAnalysisResult(
        classification = when (classification) {
            "REGULAR" -> CustomerClassification.REGULAR
            "HIGH_RISK" -> CustomerClassification.HIGH_RISK
            "PREFERENTIAL" -> CustomerClassification.PREFERENTIAL
            "NO_INFORMATION" -> CustomerClassification.NO_INFORMATION
            else -> throw IllegalArgumentException("Unknown classification: $classification")
        }
    )
}