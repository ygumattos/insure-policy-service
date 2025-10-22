package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.FraudAnalysisResult

interface FraudAnalysisPort {
    fun analyzeFraud(customerId: String): FraudAnalysisResult
}