package br.com.itau.adapters.messaging.kafka.producers.mappers

import br.com.itau.adapters.messaging.kafka.producers.dto.PolicyStatusChangedEventProducerDTO
import br.com.itau.domain.entities.PolicyRequest.PolicyStatusSnapshot
import br.com.itau.domain.events.PolicyStatusChangedEvent

object producerMappers {
    fun PolicyStatusSnapshot.toProducerDTO(): PolicyStatusChangedEvent {
        val producerEvent = PolicyStatusChangedEventProducerDTO(
            id = this.id,
            status = this.status,
            paymentConfirmation = this.paymentConfirmation,
            subscriptionAuthorization = this.subscriptionAutorization,
            finishedAt = this.finishedAt
        )

        return PolicyStatusChangedEvent(
            eventData = producerEvent
        )
    }

}