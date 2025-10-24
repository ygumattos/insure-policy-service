package br.com.itau.adapters.messaging.kafka.consumers

import br.com.itau.adapters.messaging.kafka.config.KafkaTopicsConfig.Companion.SUBSCRIPTION_EVENTS
import br.com.itau.adapters.messaging.kafka.consumers.dto.SubscriptionEventMessage
import br.com.itau.adapters.messaging.kafka.consumers.mappers.eventMappers.toCommand
import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.ProcessValidationEventUseCase
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class SubscriptionEventConsumer(
    private val processValidationEvent: ProcessValidationEventUseCase
): Logging {
    @KafkaListener(
        topics = [SUBSCRIPTION_EVENTS],
        groupId = "insure-policy-service",
        containerFactory = "subscriptionKafkaListenerContainerFactory"
    )
    fun onMessage(
        @Payload message: SubscriptionEventMessage,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String?
    ) {
        log.info("subscription-event received key={} message={}", key, message)
        processValidationEvent.handle(message.toCommand())
    }
}