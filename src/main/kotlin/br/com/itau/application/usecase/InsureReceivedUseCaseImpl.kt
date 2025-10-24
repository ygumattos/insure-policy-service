package br.com.itau.application.usecase

import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.EvaluatePolicyUseCase
import br.com.itau.application.ports.inputs.InsureReceivedUseCase
import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.application.ports.outputs.FraudAnalysis
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.PolicyStatus
import org.springframework.stereotype.Service
import br.com.itau.application.exceptions.PolicyExceptions.*
import br.com.itau.application.ports.outputs.PolicyStatusChangedEventProducer

@Service
class InsureReceivedUseCaseImpl(
    private val fraudAnalysis: FraudAnalysis,
    private val policyRequestRepository: PolicyRequestRepository,
    private val evaluatePolicyUseCase: EvaluatePolicyUseCase,
    private val policyStatusUseCase: PolicyStatusUseCase,
    private val policyStatusChangedEventProducer: PolicyStatusChangedEventProducer
) : InsureReceivedUseCase, Logging {
    
    override fun create(command: PolicyCommand): PolicyRequest {
        log.info("Starting policy creation for customer: {}", command.customerId)

        try {
            val policyRequest = PolicyRequest.create(command)
            log.debug("PolicyRequest created with ID: {}", policyRequest.id)

            policyRequestRepository.save(policyRequest)
            log.debug("PolicyRequest initially saved")

            val fraudResult = fraudAnalysis.analyzeFraud(command.customerId)
            log.info("Fraud analysis completed. Classification: {}", fraudResult.classification)

            val policyRequestWithClassification = policyRequest.putClassification(fraudResult.classification)
            val policyRequestValidated = evaluatePolicyUseCase.execute(policyRequestWithClassification)
            log.debug("Policy validation completed. Status: {}", policyRequestValidated.status)

            if (policyRequestValidated.status == PolicyStatus.REJECTED) {
                log.warn("Policy validation rejected. Status: {}", policyRequestValidated.status)
                policyRequestRepository.save(policyRequestValidated)
                return policyRequestValidated
            }

            log.info("Policy validated, moving to PENDING state")
            policyStatusUseCase.moveToPending(policyRequestValidated).also {
                policyRequestRepository.save(policyRequestValidated)
            }

            policyStatusChangedEventProducer.publish(policyRequestValidated.createSnapShot())
            log.info("Policy send to StatusChanged queue successfully. ID: {}", policyRequestValidated.id)

            return policyRequestValidated

        } catch (e: Exception) {
            log.error(
                "Failed to create policy for customer: {}. Error: {}",
                command.customerId,
                e.message,
                e
            )
            throw when (e) {
                is IllegalArgumentException -> PolicyCreationException("Invalid policy data: ${e.message}", e)
                is IllegalStateException -> PolicyCreationException("Policy business rule violation: ${e.message}", e)
                else -> PolicyCreationException("Unexpected error during policy creation: ${e.message}", e)
            }
        }
    }
    override fun getById(policyRequestId: String): PolicyRequest {
        log.info("Fetching policy request with ID: {}", policyRequestId)

        return try {
            policyRequestRepository.findById(policyRequestId)?.also {
                log.debug("Policy request found: {}", it.id)
            } ?: run {
                log.warn("Policy request not found with ID: {}", policyRequestId)
                throw PolicyNotFoundException("Policy request not found with id: $policyRequestId")
            }
        } catch (e: IllegalArgumentException) {
            log.warn("Invalid policy request ID format: {}", policyRequestId)
            throw PolicyNotFoundException("Invalid policy request ID format: $policyRequestId", e)
        } catch (e: PolicyNotFoundException) {
            throw e
        } catch (e: Exception) {
            log.error("Unexpected error fetching policy request: {}", policyRequestId, e)
            throw PolicyRetrievalException("Failed to retrieve policy request: ${e.message}", e)
        }
    }

}