package br.com.itau.application.ports.outputs

import br.com.itau.adapters.repositories.views.PolicyStatusWithHistoryView
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.PolicyStatus
import java.time.Instant

interface PolicyRequestRepository {
    fun save(policyRequest: PolicyRequest): PolicyRequest
    fun findById(id: String): PolicyRequest?
    /**
     * Retorna snapshot com APENAS 1 coleção (history) para evitar MultipleBagFetch.
     * Se não existir, retorna null.
     */
    fun findStatusWithHistoryById(id: String): PolicyRequest.PolicyStatusSnapshot?

    /**
     * Atualiza atomically paymentConfirmation + status + finishedAt.
     * Retorna quantidade de linhas afetadas (0 => id não existe).
     */
    fun updatePaymentAndStatus(
        id: String,
        payment: Boolean?,
        status: PolicyStatus,
        finishedAt: Instant?
    ): Int

    /**
     * Atualiza apenas paymentConfirmation, mantendo status inalterado.
     */
    fun updatePaymentOnly(id: String, payment: Boolean): Int

    /**
     * Acrescenta item no histórico de status (sem tocar coleções carregadas).
     */
    fun appendStatusHistory(id: String, newStatus: PolicyStatus, changedAt: Instant): Int
}