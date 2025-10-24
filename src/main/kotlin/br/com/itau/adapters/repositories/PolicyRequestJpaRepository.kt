package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.entities.PolicyRequestEntity
import br.com.itau.domain.enums.PolicyStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface PolicyRequestJpaRepository : JpaRepository<PolicyRequestEntity, String> {
    @Query("""
        select distinct p from PolicyRequestEntity p
        left join fetch p.statusHistory h
        where p.id = :id
    """)
    fun findWithHistoryById(@Param("id") id: String): Optional<PolicyRequestEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update PolicyRequestEntity p
      set p.paymentConfirmation = :payment,
          p.status = :status,
          p.finishedAt = :finishedAt
      where p.id = :id
    """)
    fun updatePaymentAndStatus(
        @Param("id") id: String,
        @Param("payment") payment: Boolean?,
        @Param("status") status: PolicyStatus,
        @Param("finishedAt") finishedAt: Instant?
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update PolicyRequestEntity p
      set p.paymentConfirmation = :payment
      where p.id = :id
    """)
    fun updatePaymentOnly(
        @Param("id") id: String,
        @Param("payment") payment: Boolean
    ): Int
}