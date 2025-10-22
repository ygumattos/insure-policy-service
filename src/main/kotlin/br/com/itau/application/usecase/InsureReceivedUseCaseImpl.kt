package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.InsureReceivedUseCase
import br.com.itau.application.ports.outputs.FraudAnalysisPort
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.valueobjects.PolicyRequestId
import org.springframework.stereotype.Service

@Service
class InsureReceivedUseCaseImpl(
    private val fraudAnalysisPort: FraudAnalysisPort,
   private val policyRequestRepository: PolicyRequestRepository
) : InsureReceivedUseCase {
    override fun execute(command: PolicyCommand): PolicyRequest {

        val policyRequest = PolicyRequest.create(command)

        policyRequestRepository.save(policyRequest)

        val fraudResult = fraudAnalysisPort.analyzeFraud(command.customerId)

        policyRequest.validate()

        return policyRequest
    }
}