package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.mappers.repositoryMappers.toDomain
import br.com.itau.adapters.repositories.mappers.repositoryMappers.toEntity
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyRequest
import org.springframework.stereotype.Repository

@Repository
class PolicyRequestRepositoryImpl(
    private val jpaRepository: PolicyRequestJpaRepository
) : PolicyRequestRepository {

    override fun save(policyRequest: PolicyRequest): PolicyRequest {
        val entity = policyRequest.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: String): PolicyRequest? {
        return jpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByCustomerId(customerId: String): List<PolicyRequest> {
        return jpaRepository.findByCustomerId(customerId).map { it.toDomain() }
    }
}