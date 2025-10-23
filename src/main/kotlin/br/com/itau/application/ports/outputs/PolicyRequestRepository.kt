package br.com.itau.application.ports.outputs

import br.com.itau.domain.entities.PolicyRequest

interface PolicyRequestRepository {
    fun save(policyRequest: PolicyRequest): PolicyRequest
    fun findById(id: String): PolicyRequest?
    fun findByCustomerId(customerId: String): List<PolicyRequest>
}