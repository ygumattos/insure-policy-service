package br.com.itau.domain.entities

import br.com.itau.domain.enums.CustomerClassification

data class FraudAnalysisResult(
    val classification: CustomerClassification,
)