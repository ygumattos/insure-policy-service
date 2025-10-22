package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.valueobjects.PolicyRequestId
import java.util.*

interface PolicyRequestRepository {
    fun save(policyRequest: PolicyRequest): PolicyRequest
    fun findById(id: PolicyRequestId): PolicyRequest?
    fun findByCustomerId(customerId: String): List<PolicyRequest>
}