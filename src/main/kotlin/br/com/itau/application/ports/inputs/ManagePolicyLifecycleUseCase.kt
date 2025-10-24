package br.com.itau.application.ports.inputs

import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest

interface ManagePolicyLifecycleUseCase {
    fun create(command: PolicyCommand): PolicyRequest
    fun getById(policyRequestId: String): PolicyRequest
    fun cancel(policyRequestId: String)
}