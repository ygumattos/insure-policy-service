package br.com.itau.domain.events

import br.com.itau.adapters.messaging.kafka.producers.dto.PolicyStatusChangedEventProducerDTO
import java.time.Instant
import java.util.*

data class PolicyStatusChangedEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventData: PolicyStatusChangedEventProducerDTO,
    val occurredAt: Instant = Instant.now(),
    val version: Long = 1,
)