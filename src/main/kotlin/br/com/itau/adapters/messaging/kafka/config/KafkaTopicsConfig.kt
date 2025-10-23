package br.com.itau.adapters.messaging.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaTopicsConfig {

    companion object {
        const val POLICY_STATUS_CHANGED = "policy-status-changed"
        const val PAYMENT_EVENTS = "payment-events"
    }

    @Bean
    fun policyStatusChangedTopic(): NewTopic =
        NewTopic(POLICY_STATUS_CHANGED, 1, 1.toShort())

    @Bean
    fun paymentEventsTopic(): NewTopic =
        NewTopic(PAYMENT_EVENTS, 1, 1.toShort())
}