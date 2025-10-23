package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.EvaluatePolicyUseCase
import br.com.itau.application.ports.inputs.InsureReceivedUseCase
import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.application.ports.outputs.FraudAnalysisPort
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import org.slf4j.LoggerFactory
import br.com.itau.domain.enums.PolicyStatus
import org.springframework.stereotype.Service
import br.com.itau.application.exceptions.PolicyExceptions.*

@Service
class InsureReceivedUseCaseImpl(
    private val fraudAnalysisPort: FraudAnalysisPort,
    private val policyRequestRepository: PolicyRequestRepository,
    private val evaluatePolicyUseCase: EvaluatePolicyUseCase,
    private val policyStatusUseCase: PolicyStatusUseCase
) : InsureReceivedUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun create(command: PolicyCommand): PolicyRequest {
        logger.info("Starting policy creation for customer: {}", command.customerId)

        return try {
            val policyRequest = PolicyRequest.create(command)
            logger.debug("PolicyRequest created with ID: {}", policyRequest.id)

            policyRequestRepository.save(policyRequest)
            logger.debug("PolicyRequest initially saved")

            val fraudResult = fraudAnalysisPort.analyzeFraud(command.customerId)
            logger.info("Fraud analysis completed. Classification: {}", fraudResult.classification)

            val policyRequestWithClassification = policyRequest.putClassification(fraudResult.classification)
            val policyRequestValidated = evaluatePolicyUseCase.execute(policyRequestWithClassification)
            logger.debug("Policy validation completed. Status: {}", policyRequestValidated.status)

            val finalPolicyRequest = if (policyRequestValidated.status == PolicyStatus.VALIDATED) {
                logger.info("Policy validated, moving to PENDING state")
                policyStatusUseCase.moveToPending(policyRequestValidated)
            } else {
                logger.warn("Policy validation failed. Status: {}", policyRequestValidated.status)
                policyRequestValidated
            }

            policyRequestRepository.save(finalPolicyRequest)
            logger.info("Policy creation completed successfully. ID: {}", finalPolicyRequest.id)

            finalPolicyRequest

        } catch (e: Exception) {
            logger.error(
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
        logger.info("Fetching policy request with ID: {}", policyRequestId)

        return try {
            policyRequestRepository.findById(policyRequestId)?.also {
                logger.debug("Policy request found: {}", it.id)
            } ?: run {
                logger.warn("Policy request not found with ID: {}", policyRequestId)
                throw PolicyNotFoundException("Policy request not found with id: $policyRequestId")
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid policy request ID format: {}", policyRequestId)
            throw PolicyNotFoundException("Invalid policy request ID format: $policyRequestId", e)
        } catch (e: PolicyNotFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error fetching policy request: {}", policyRequestId, e)
            throw PolicyRetrievalException("Failed to retrieve policy request: ${e.message}", e)
        }
    }

}