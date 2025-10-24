package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.PolicyRequest
import java.time.Instant

interface PolicyRequestRepository {
    fun save(policyRequest: PolicyRequest): PolicyRequest
    fun findById(id: String): PolicyRequest?
    fun findStatusWithHistoryById(id: String): PolicyRequest.PolicyStatusSnapshot?
    fun updatePaymentAndStatus(
        id: String,
        payment: Boolean?,
        status: String,
        finishedAt: Instant?
    )
    fun appendStatusHistory(id: String, newStatus: String, changedAt: Instant)
    fun updateSubscriptionAndStatus(
        id: String,
        subscription: Boolean?,
        status: String,
        finishedAt: Instant?
    )
}