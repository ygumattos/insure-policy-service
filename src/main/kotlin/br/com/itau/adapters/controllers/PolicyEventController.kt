package br.com.itau.adapters.controllers

import br.com.itau.adapters.messaging.kafka.producers.PolicyEventProducer
import br.com.itau.domain.events.PolicyStatusChangedEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/events")
class PolicyEventController(
    private val producer: PolicyEventProducer
) {
    @PostMapping("/policy-status")
    fun publish(@RequestBody event: PolicyStatusChangedEvent): ResponseEntity<Void> {
        producer.publish(event)
        return ResponseEntity.accepted().build()
    }
}