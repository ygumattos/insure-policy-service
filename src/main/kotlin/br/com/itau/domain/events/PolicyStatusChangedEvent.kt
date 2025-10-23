package br.com.itau.domain.events

import java.time.Instant
import java.util.*

data class PolicyStatusChangedEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val policyId: String,
    val eventType: String,          // RECEIVED, VALIDATED, PENDING, APPROVED, REJECTED, CANCELLED
    val occurredAt: Instant = Instant.now(),
    val version: Long = 1,
    val classification: String? = null
)