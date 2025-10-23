package br.com.itau.adapters.messaging.kafka.consumers

import br.com.itau.adapters.messaging.kafka.config.KafkaTopicsConfig.Companion.PAYMENT_EVENTS
import br.com.itau.domain.events.PaymentEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [PAYMENT_EVENTS],
        groupId = "insure-policy-service"   // mesmo group p/ todos consumidores dessa app
    )
    fun onMessage(
        @Payload payload: PaymentEvent,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String?
    ) {
        log.info("payment-event received key={} payload={}", key, payload)

        // aqui você chama seus usecases.
        // ex.: if (payload.status == "PAID") policyStatusUseCase.moveToPending(...)
        // mantive simples para não acoplar agora.
    }
}