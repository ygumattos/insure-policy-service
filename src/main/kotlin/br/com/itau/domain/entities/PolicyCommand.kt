package br.com.itau.domain.entities

import java.math.BigDecimal

data class PolicyCommand(
    val customerId: String,
    val productId: String,
    val category: String,
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
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
    }
}
