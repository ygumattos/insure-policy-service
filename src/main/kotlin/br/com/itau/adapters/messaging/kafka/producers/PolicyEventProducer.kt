package br.com.itau.adapters.messaging.kafka.producers

import br.com.itau.adapters.messaging.kafka.config.KafkaTopicsConfig.Companion.POLICY_STATUS_CHANGED
import br.com.itau.domain.events.PolicyStatusChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component

@Component
class PolicyEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, PolicyStatusChangedEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(event: PolicyStatusChangedEvent) {
        val key = event.policyId

        kafkaTemplate.send(POLICY_STATUS_CHANGED, key, event)
            .whenComplete { result: SendResult<String, PolicyStatusChangedEvent>?, ex: Throwable? ->
                if (ex != null) {
                    log.error("failed to publish policy-event key={} error={}", key, ex.message, ex)
                } else {
                    val md = result!!.recordMetadata
                    log.info(
                        "policy-event published topic={} partition={} offset={} key={}",
                        md.topic(), md.partition(), md.offset(), key
                    )
                }
            }
    }
}