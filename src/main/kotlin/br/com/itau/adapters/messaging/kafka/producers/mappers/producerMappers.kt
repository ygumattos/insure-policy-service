package br.com.itau.adapters.messaging.kafka.producers.mappers

import br.com.itau.adapters.messaging.kafka.producers.dto.PolicyStatusChangedEventProducerDTO
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.events.PolicyStatusChangedEvent

object producerMappers {
    fun PolicyRequest.toProducerDTO(): PolicyStatusChangedEvent {
        val producerEvent = PolicyStatusChangedEventProducerDTO(
            id = this.id,
            customerId = this.customerId,
            productId = this.productId,
            category = this.category.name,
            status = this.status.name,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            classification = this.classification!!.name
        )

        return PolicyStatusChangedEvent(
            eventData = producerEvent
        )
    }

}