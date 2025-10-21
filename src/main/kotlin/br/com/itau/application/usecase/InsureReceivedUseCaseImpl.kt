package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.InsureReceivedUseCase
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.valueobjects.PolicyRequestId
import org.springframework.stereotype.Service

@Service
class InsureReceivedUseCaseImpl(
   // private val policyRequestRepository: PolicyRequestRepository
) : InsureReceivedUseCase {

    override fun execute(command: PolicyCommand): PolicyRequestId {
        // TODO: Implementar a lógica
        // 1. Criar PolicyRequest a partir do command
        // 2. Criar waremock para API de Fraude
        // 3. Persistir no repositório
        // 4. Retornar o ID
        return TODO("Provide the return value")
    }
}