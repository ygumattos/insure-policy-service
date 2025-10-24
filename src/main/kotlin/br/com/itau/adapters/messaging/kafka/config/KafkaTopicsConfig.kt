package br.com.itau.adapters.messaging.kafka.config

import br.com.itau.adapters.messaging.kafka.consumers.dto.PaymentEventMessage
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

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

    @Bean
    fun paymentKafkaListenerContainerFactory(
        @Value("\${spring.kafka.bootstrap-servers}") brokers: String
    ): ConcurrentKafkaListenerContainerFactory<String, PaymentEventMessage> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ConsumerConfig.GROUP_ID_CONFIG to "insure-policy-service",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            JsonDeserializer.TRUSTED_PACKAGES to "br.com.itau.*",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false
        )
        val factory = ConcurrentKafkaListenerContainerFactory<String, PaymentEventMessage>()
        factory.consumerFactory = DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            JsonDeserializer(PaymentEventMessage::class.java)
        )
        return factory
    }
}