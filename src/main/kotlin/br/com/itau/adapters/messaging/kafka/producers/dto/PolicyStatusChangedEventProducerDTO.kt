package br.com.itau.adapters.messaging.kafka.producers.dto

import br.com.itau.domain.enums.PolicyStatus
import java.time.Instant

data class PolicyStatusChangedEventProducerDTO(
    val id: String,
    val status: PolicyStatus,
    val paymentConfirmation: Boolean?,
    val subscriptionAuthorization: Boolean?,
    val finishedAt: Instant?
)