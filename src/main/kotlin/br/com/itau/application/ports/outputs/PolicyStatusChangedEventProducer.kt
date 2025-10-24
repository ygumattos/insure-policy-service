package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.PolicyRequest.PolicyStatusSnapshot

interface PolicyStatusChangedEventProducer {
    fun publish(event: PolicyStatusSnapshot)
}