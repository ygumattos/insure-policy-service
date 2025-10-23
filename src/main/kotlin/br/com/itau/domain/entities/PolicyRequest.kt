package br.com.itau.domain.entities

import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.domain.valueobjects.PolicyRequestId
import java.math.BigDecimal
import java.time.Instant

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
    val history: MutableList<StatusHistory>
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
                history = mutableListOf(initialHistory)
            )
        }
    }

    fun putClassification(classification: CustomerClassification): PolicyRequest {
        this.classification = classification
        return this
    }

    fun canBeCancelled(): Boolean =
        status != PolicyStatus.APPROVED && status != PolicyStatus.REJECTED && status != PolicyStatus.CANCELLED

    fun isFinalState(): Boolean =
        status == PolicyStatus.APPROVED || status == PolicyStatus.REJECTED || status == PolicyStatus.CANCELLED

//
//    fun validate(classification: CustomerClassification): PolicyRequest {
//        require(status == PolicyStatus.RECEIVED) {
//            "Only RECEIVED policies can be validated. Current status: $status"
//        }
//
//        this.classification = classification
//        this.status = PolicyStatus.VALIDATED
//        this.history.add(StatusHistory(PolicyStatus.VALIDATED, Instant.now()))
//        return this
//    }
//
//    fun moveToPending(): PolicyRequest {
//        require(status == PolicyStatus.VALIDATED) {
//            "Only VALIDATED policies can move to PENDING. Current status: $status"
//        }
//
//        this.status = PolicyStatus.PENDING
//        this.history.add(StatusHistory(PolicyStatus.PENDING, Instant.now()))
//        return this
//    }
//
//    fun approve(): PolicyRequest {
//        require(status == PolicyStatus.PENDING) {
//            "Only PENDING policies can be APPROVED. Current status: $status"
//        }
//
//        this.status = PolicyStatus.APPROVED
//        this.finishedAt = Instant.now()
//        this.history.add(StatusHistory(PolicyStatus.APPROVED, Instant.now()))
//        return this
//    }
//
//    fun reject(): PolicyRequest {
//        require(status == PolicyStatus.RECEIVED || status == PolicyStatus.VALIDATED || status == PolicyStatus.PENDING) {
//            "Cannot reject policy with status: $status"
//        }
//
//        this.status = PolicyStatus.REJECTED
//        this.finishedAt = Instant.now()
//        this.history.add(StatusHistory(PolicyStatus.REJECTED, Instant.now()))
//        return this
//    }
//
//    fun cancel(): PolicyRequest {
//        require(status != PolicyStatus.APPROVED && status != PolicyStatus.REJECTED) {
//            "Cannot cancel policy with status: $status"
//        }
//        require(status != PolicyStatus.CANCELLED) {
//            "Policy is already CANCELLED"
//        }
//
//        this.status = PolicyStatus.CANCELLED
//        this.finishedAt = Instant.now()
//        this.history.add(StatusHistory(PolicyStatus.CANCELLED, Instant.now()))
//        return this
//    }
}