package br.com.itau.domain.entities

import br.com.itau.domain.enums.PolicyStatus
import java.time.Instant

data class StatusHistory(
    val status: PolicyStatus,
    val timestamp: Instant
)
