package br.com.itau.application.ports.inputs

import br.com.itau.domain.entities.PolicyRequest

interface PolicyStatusUseCase {

    fun validate(policy: PolicyRequest): PolicyRequest
    fun moveToPending(policy: PolicyRequest): PolicyRequest
    fun approve(policy: PolicyRequest): PolicyRequest
    fun reject(policy: PolicyRequest): PolicyRequest
    fun cancel(policy: PolicyRequest): PolicyRequest
}