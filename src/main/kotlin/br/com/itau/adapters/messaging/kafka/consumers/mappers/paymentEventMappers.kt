package br.com.itau.adapters.messaging.kafka.consumers.mappers

import br.com.itau.adapters.messaging.kafka.consumers.dto.PaymentEventMessage
import br.com.itau.domain.events.ProcessPaymentCommand

object paymentEventMappers {
    fun PaymentEventMessage.toCommand(): ProcessPaymentCommand =
        ProcessPaymentCommand(
            policyRequestId = this.id,
            paymentConfirmation = this.paymentConfirmation
        )
}
