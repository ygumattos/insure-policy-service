package br.com.itau.application.ports.inputs

import br.com.itau.domain.entities.PolicyRequest

interface EvaluatePolicyUseCase {
    fun execute(policyRequest: PolicyRequest): PolicyRequest
}