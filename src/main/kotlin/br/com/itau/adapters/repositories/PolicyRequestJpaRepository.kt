package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.entities.PolicyRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PolicyRequestJpaRepository : JpaRepository<PolicyRequestEntity, String> {
    fun findByCustomerId(customerId: String): List<PolicyRequestEntity>
}