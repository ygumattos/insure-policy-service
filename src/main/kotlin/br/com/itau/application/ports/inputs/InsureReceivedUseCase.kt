package br.com.itau.application.ports.inputs

import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.valueobjects.PolicyRequestId

interface InsureReceivedUseCase {
    fun execute(command: PolicyCommand): PolicyRequestId
}