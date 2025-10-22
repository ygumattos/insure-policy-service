package br.com.itau.adapters.controllers.mappers

import br.com.itau.adapters.controllers.dtos.CreatePolicyRequestDto
import br.com.itau.adapters.controllers.dtos.CreatePolicyResponseDto
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import java.math.BigDecimal
import java.time.Instant

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

    fun PolicyRequest.toDTO(customerId: String) : CreatePolicyResponseDto =
        CreatePolicyResponseDto(
            id = this.id.toString(),
            customerId = this.customerId,
            productId = this.productId,
            category = this.category,
            salesChannel = this.salesChannel,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            coverages = this.coverages,
            assistances = this.assistances,
            status = this.status,
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            history = this.history
        )

}