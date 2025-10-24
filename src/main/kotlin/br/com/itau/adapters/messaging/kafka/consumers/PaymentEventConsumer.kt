package br.com.itau.adapters.messaging.kafka.consumers

import br.com.itau.adapters.messaging.kafka.config.KafkaTopicsConfig.Companion.PAYMENT_EVENTS
import br.com.itau.adapters.messaging.kafka.consumers.dto.PaymentEventMessage
import br.com.itau.adapters.messaging.kafka.consumers.mappers.paymentEventMappers.toCommand
import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.ProcessPaymentEventUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val useCase: ProcessPaymentEventUseCase
): Logging {

    @KafkaListener(
        topics = [PAYMENT_EVENTS],
        groupId = "insure-policy-service",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    fun onMessage(
        @Payload message: PaymentEventMessage,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String?
    ) {
        log.info("payment-event received key={} message={}", key, message)
        useCase.handle(message.toCommand())
    }
}
