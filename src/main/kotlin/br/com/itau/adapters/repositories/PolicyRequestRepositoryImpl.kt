package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.mappers.repositoryMappers.toDomain
import br.com.itau.adapters.repositories.mappers.repositoryMappers.toEntity
import br.com.itau.adapters.repositories.mappers.repositoryMappers.toSnapshot
import br.com.itau.adapters.repositories.views.PolicyStatusWithHistoryView
import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.PolicyStatus
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class PolicyRequestRepositoryImpl(
    private val jpaRepository: PolicyRequestJpaRepository,
    private val jpaHistoryRepository: StatusHistoryJpaRepository
) : PolicyRequestRepository, Logging {

    override fun save(policyRequest: PolicyRequest): PolicyRequest {
        val entity = policyRequest.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: String): PolicyRequest? {
        val entity = jpaRepository.findWithHistoryById(id).orElse(null) ?: return null
        return entity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findStatusWithHistoryById(id: String): PolicyRequest.PolicyStatusSnapshot? {
        val entity = jpaRepository.findWithHistoryById(id).orElse(null) ?: return null
        return entity.toSnapshot()
    }

    @Transactional
    override fun updatePaymentAndStatus(
        id: String,
        payment: Boolean?,
        status: PolicyStatus,
        finishedAt: Instant?
    ): Int = jpaRepository.updatePaymentAndStatus(id, payment, status, finishedAt)

    @Transactional
    override fun updatePaymentOnly(id: String, payment: Boolean): Int =
        jpaRepository.updatePaymentOnly(id, payment)

    @Transactional
    override fun appendStatusHistory(id: String, newStatus: PolicyStatus, changedAt: Instant): Int =
        jpaHistoryRepository.insertOne(id, newStatus.name, changedAt)
}