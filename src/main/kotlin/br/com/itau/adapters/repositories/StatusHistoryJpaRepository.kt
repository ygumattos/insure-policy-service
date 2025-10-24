package br.com.itau.adapters.repositories

import br.com.itau.adapters.repositories.entities.StatusHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface StatusHistoryJpaRepository : JpaRepository<StatusHistoryEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      insert into policy_status_history  (policy_request_id, status, changed_at)
      values (:policyId, :status, :changedAt)
    """, nativeQuery = true)
    fun insertOne(
        @Param("policyId") policyId: String,
        @Param("status") status: String,
        @Param("changedAt") changedAt: Instant
    ): Int
}