package br.com.itau.domain.views

import br.com.itau.domain.enums.PolicyStatus
import java.time.Instant

data class PolicyStatusWithHistoryView(
    val id: String,
    val status: PolicyStatus,
    val paymentConfirmation: Boolean?,
    val subscriptionAutorization: Boolean?,
    val finishedAt: Instant?,
    val history: List<HistoryItem>
) {
    data class HistoryItem(
        val status: PolicyStatus,
        val changedAt: Instant
    )
}