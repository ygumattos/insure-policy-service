package br.com.itau.adapters.messaging.kafka.producers

import br.com.itau.adapters.messaging.kafka.config.KafkaTopicsConfig.Companion.POLICY_STATUS_CHANGED
import br.com.itau.adapters.messaging.kafka.producers.mappers.producerMappers.toProducerDTO
import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.outputs.PolicyStatusChangedEventProducer
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.events.PolicyStatusChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component

@Component
class PolicyStatusChangedEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, PolicyStatusChangedEvent>
): PolicyStatusChangedEventProducer, Logging {

    override fun publish(event: PolicyRequest) {
        val policyEventProducerData = event.toProducerDTO()
        val key = policyEventProducerData.eventData.id

        kafkaTemplate.send(POLICY_STATUS_CHANGED, key, policyEventProducerData)
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