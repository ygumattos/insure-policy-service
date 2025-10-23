package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.EvaluatePolicyUseCase
import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.domain.entities.PolicyEvaluationConfig
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EvaluatePolicyUseCaseImpl(
    private val config: PolicyEvaluationConfig,
    private val policyUseCase: PolicyStatusUseCase
) : EvaluatePolicyUseCase {

    override fun execute(policyRequest: PolicyRequest): PolicyRequest {
        return when (policyRequest.classification) {
            CustomerClassification.REGULAR -> evaluateRegular(policyRequest)
            CustomerClassification.HIGH_RISK -> evaluateHighRisk(policyRequest)
            CustomerClassification.PREFERENTIAL -> evaluatePreferential(policyRequest)
            CustomerClassification.NO_INFORMATION -> evaluateNoInformation(policyRequest)
            else -> policyUseCase.reject(policyRequest)
        }
    }

    private fun evaluateRegular(policy: PolicyRequest): PolicyRequest {
        val limit = when (policy.category) {
            InsuranceCategory.VIDA, InsuranceCategory.RESIDENCIAL -> config.regular.vidaResidencial
            InsuranceCategory.AUTO -> config.regular.auto
            else -> config.regular.outros
        }

        return evaluateByLimit(policy, limit)
    }

    private fun evaluateHighRisk(policy: PolicyRequest): PolicyRequest {
        val limit = when (policy.category) {
            InsuranceCategory.AUTO -> config.highRisk.auto
            InsuranceCategory.RESIDENCIAL -> config.highRisk.residencial
            else -> config.highRisk.outros
        }

        return evaluateByLimit(policy, limit)
    }

    private fun evaluatePreferential(policy: PolicyRequest): PolicyRequest {
        val (limit, inclusive) = when (policy.category) {
            InsuranceCategory.VIDA -> config.preferential.vida to false
            InsuranceCategory.AUTO, InsuranceCategory.RESIDENCIAL -> config.preferential.autoResidencial to false
            else -> config.preferential.outros to true
        }
        return evaluateByLimit(policy, limit, inclusive)
    }

    private fun evaluateNoInformation(policy: PolicyRequest): PolicyRequest {
        val limit = when (policy.category) {
            InsuranceCategory.VIDA, InsuranceCategory.RESIDENCIAL -> config.noInfo.vidaResidencial
            InsuranceCategory.AUTO -> config.noInfo.auto
            else -> config.noInfo.outros
        }

        return evaluateByLimit(policy, limit)
    }

    private fun evaluateByLimit(
        policy: PolicyRequest,
        limit: BigDecimal?,
        inclusive: Boolean = true
    ): PolicyRequest {
        val withinLimit = if (inclusive)
            policy.insuredAmount <= limit
        else
            policy.insuredAmount < limit

        return if (withinLimit)
            policyUseCase.validate(policy)
        else
            policyUseCase.reject(policy)
    }
}