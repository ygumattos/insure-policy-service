package br.com.itau.adapters.controllers.mappers

import br.com.itau.adapters.controllers.dtos.CreatePolicyRequestDto
import br.com.itau.adapters.controllers.dtos.CreatePolicyResponseDto
import br.com.itau.adapters.controllers.dtos.PolicyResponseDto
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest

object createPolicyMappers {
    fun CreatePolicyRequestDto.toDomain() : PolicyCommand =
        PolicyCommand(
            customerId = this.customerId,
            productId = this.productId,
            category = this.category,
            salesChannel = this.salesChannel,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            coverages = this.coverages,
            assistances = this.assistances
        )

    fun PolicyRequest.toCreateDTO(): CreatePolicyResponseDto =
        CreatePolicyResponseDto(
            id = this.id,
            createdAt = this.createdAt
        )

    fun PolicyRequest.toDTO() : PolicyResponseDto =
        PolicyResponseDto(
            id = this.id,
            customerId = this.customerId,
            productId = this.productId,
            category = this.category.toString(),
            salesChannel = this.salesChannel,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            coverages = this.coverages,
            assistances = this.assistances,
            classification = this.classification?.name,
            status = this.status,
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            history = this.history
        )

}