package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.entities.StatusHistory
import br.com.itau.domain.enums.PolicyStatus
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PolicyStatusUseCaseImpl(): PolicyStatusUseCase {

    override fun validate(policy: PolicyRequest): PolicyRequest {
        require(policy.status == PolicyStatus.RECEIVED) {
            "Only RECEIVED policies can be validated. Current status: ${policy.status}"
        }

        return transition(policy, PolicyStatus.VALIDATED)
    }

    override fun moveToPending(policy: PolicyRequest): PolicyRequest {
        require(policy.status == PolicyStatus.VALIDATED) {
            "Only VALIDATED policies can move to PENDING. Current status: ${policy.status}"
        }

        return transition(policy, PolicyStatus.PENDING)
    }

    override fun approve(policy: PolicyRequest): PolicyRequest {
        require(policy.status == PolicyStatus.PENDING) {
            "Only PENDING policies can be APPROVED. Current status: ${policy.status}"
        }

        return transition(policy, PolicyStatus.APPROVED, finished = true)
    }

    override fun reject(policy: PolicyRequest): PolicyRequest {
        require(policy.status in listOf(PolicyStatus.RECEIVED, PolicyStatus.VALIDATED, PolicyStatus.PENDING)) {
            "Cannot reject policy with status: ${policy.status}"
        }

        return transition(policy, PolicyStatus.REJECTED, finished = true)
    }

    override fun cancel(policy: PolicyRequest): PolicyRequest {
        require(policy.status !in listOf(PolicyStatus.APPROVED, PolicyStatus.REJECTED, PolicyStatus.CANCELLED)) {
            "Cannot cancel policy with status: ${policy.status}"
        }

        return transition(policy, PolicyStatus.CANCELLED, finished = true)
    }

    private fun transition(policy: PolicyRequest, newStatus: PolicyStatus, finished: Boolean = false): PolicyRequest {
        policy.status = newStatus
        policy.history.add(StatusHistory(newStatus, Instant.now()))
        if (finished) policy.finishedAt = Instant.now()
        return policy
    }
}