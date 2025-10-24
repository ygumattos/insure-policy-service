package br.com.itau.domain.entities

import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.domain.valueobjects.PolicyRequestId
import java.math.BigDecimal
import java.time.Instant
import kotlin.String

class PolicyRequest(
    val id: String,
    val customerId: String,
    val productId: String,
    val category: InsuranceCategory,
    val salesChannel: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    var classification: CustomerClassification?,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    var status: PolicyStatus,
    val createdAt: Instant,
    var finishedAt: Instant?,
    val history: MutableList<StatusHistory>,
    val validatedFlags: Flags,
) {

    companion object {
        fun create(command: PolicyCommand): PolicyRequest {
            require(command.insuredAmount > BigDecimal.ZERO) { "Insured amount must be positive" }
            require(command.totalMonthlyPremiumAmount > BigDecimal.ZERO) { "Premium amount must be positive" }
            require(command.coverages.isNotEmpty()) { "At least one coverage is required" }

            val initialHistory = StatusHistory(PolicyStatus.RECEIVED, Instant.now())

            return PolicyRequest(
                id = PolicyRequestId.generate().toString(),
                customerId = command.customerId,
                productId = command.productId,
                category = InsuranceCategory.from(command.category),
                salesChannel = command.salesChannel,
                paymentMethod = command.paymentMethod,
                totalMonthlyPremiumAmount = command.totalMonthlyPremiumAmount,
                insuredAmount = command.insuredAmount,
                coverages = command.coverages,
                assistances = command.assistances,
                status = PolicyStatus.RECEIVED,
                createdAt = Instant.now(),
                classification = null,
                finishedAt = null,
                history = mutableListOf(initialHistory),
                validatedFlags = Flags(
                    null,
                    null
                )
            )
        }
    }

    fun createSnapShot(): PolicyStatusSnapshot =
        PolicyStatusSnapshot(
            id = this.id,
            status = this.status,
            paymentConfirmation = this.validatedFlags.paymentConfirmation,
            subscriptionAutorization = this.validatedFlags.subscriptionAutorization,
            finishedAt = this.finishedAt,
            history = this.history
        )

    fun putClassification(classification: CustomerClassification): PolicyRequest {
        this.classification = classification
        return this
    }

    fun canBeCancelled(): Boolean =
        status != PolicyStatus.APPROVED && status != PolicyStatus.REJECTED && status != PolicyStatus.CANCELLED

    data class Flags(
        var paymentConfirmation: Boolean? = null,
        var subscriptionAutorization: Boolean? = null,
    )

    data class PolicyStatusSnapshot(
        val id: String,
        val status: PolicyStatus,
        val paymentConfirmation: Boolean?,
        val subscriptionAutorization: Boolean?,
        val finishedAt: Instant?,
        val history: MutableList<StatusHistory>
    )

}