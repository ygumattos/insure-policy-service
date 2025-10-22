package br.com.itau.domain.entities

import java.math.BigDecimal
import java.util.UUID

data class PolicyCommand(
    val customerId: String,
    val productId: String,
    val category: String,  // AUTO, LIFE, RESIDENTIAL, etc.
    val salesChannel: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>
) {
    init {
        require(totalMonthlyPremiumAmount > BigDecimal.ZERO) { "Premium amount must be positive" }
        require(insuredAmount > BigDecimal.ZERO) { "Insured amount must be positive" }
        require(customerId.toString().isNotBlank()) { "Customer ID cannot be blank" }
    }
}
