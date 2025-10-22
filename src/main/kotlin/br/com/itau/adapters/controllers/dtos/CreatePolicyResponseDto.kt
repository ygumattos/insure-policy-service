package br.com.itau.adapters.controllers.dtos

import br.com.itau.domain.entities.StatusHistory
import br.com.itau.domain.enums.PolicyStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class CreatePolicyResponseDto(
    val id: String? = null,
    val customerId: String,
    val productId: String,
    val category: String,
    val salesChannel: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    val status: PolicyStatus,
    val history: MutableList<StatusHistory>,
    val createdAt: Instant? = null,
    val finishedAt: Instant? = null
)