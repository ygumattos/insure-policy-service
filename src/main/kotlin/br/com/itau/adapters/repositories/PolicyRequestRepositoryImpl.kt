package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.entities.PolicyRequestEntity
import br.com.itau.adapters.repositories.mappers.repositoryMappers.toDomain
import br.com.itau.adapters.repositories.mappers.repositoryMappers.toEntity
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.valueobjects.PolicyRequestId
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PolicyRequestRepositoryImpl(
    private val jpaRepository: PolicyRequestJpaRepository
) : PolicyRequestRepository {

    override fun save(policyRequest: PolicyRequest): PolicyRequest {
        val entity = policyRequest.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: PolicyRequestId): PolicyRequest? {
        return jpaRepository.findById(id.toString()).orElse(null)?.toDomain()
    }

    override fun findByCustomerId(customerId: String): List<PolicyRequest> {
        return jpaRepository.findByCustomerId(customerId).map { it.toDomain() }
    }
}