package br.com.itau.application.ports.inputs

import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest

interface InsureReceivedUseCase {
    fun create(command: PolicyCommand): PolicyRequest
    fun getById(policyRequestId: String): PolicyRequest
}