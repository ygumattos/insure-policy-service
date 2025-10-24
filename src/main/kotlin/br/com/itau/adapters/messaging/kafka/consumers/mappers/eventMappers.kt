package br.com.itau.adapters.messaging.kafka.consumers.mappers

import br.com.itau.adapters.messaging.kafka.consumers.dto.PaymentEventMessage
import br.com.itau.adapters.messaging.kafka.consumers.dto.SubscriptionEventMessage
import br.com.itau.domain.events.ValidationCommand
import br.com.itau.domain.events.ValidationKind

object eventMappers {
    fun PaymentEventMessage.toCommand(): ValidationCommand =
        ValidationCommand(
            policyId = this.id,
            kind = ValidationKind.PAYMENT,
            value = this.paymentConfirmation
        )
    fun SubscriptionEventMessage.toCommand(): ValidationCommand =
        ValidationCommand(
            policyId = this.id,
            kind = ValidationKind.SUBSCRIPTION,
            value = this.subscriptionAuthorization
        )
}
