package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.PolicyRequest

interface PolicyStatusChangedEventProducer {
    fun publish(event: PolicyRequest)
}