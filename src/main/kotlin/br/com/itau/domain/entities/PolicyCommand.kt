package br.com.itau.domain.entities

import java.util.UUID

data class PolicyCommand(
    val customerId: UUID,
    val productId: UUID,
    val category: String,  // AUTO, LIFE, RESIDENTIAL, etc.
    val salesChannel: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: Double,
    val insuredAmount: Double,
    val coverages: Map<String, Double>,
    val assistances: List<String>
) {
    init {
        require(totalMonthlyPremiumAmount > 0) { "Premium amount must be positive" }
        require(insuredAmount > 0) { "Insured amount must be positive" }
        require(customerId.toString().isNotBlank()) { "Customer ID cannot be blank" }
    }
}
