package br.com.itau.adapters.repositories.mappers

import br.com.itau.adapters.repositories.entities.AssistanceEntity
import br.com.itau.adapters.repositories.entities.CoverageEntity
import br.com.itau.adapters.repositories.entities.PolicyRequestEntity
import br.com.itau.adapters.repositories.entities.StatusHistoryEntity
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.entities.StatusHistory
import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import br.com.itau.domain.enums.PolicyStatus
import java.util.UUID

object repositoryMappers {
    fun PolicyRequestEntity.toDomain(): PolicyRequest {
        return PolicyRequest(
            id = this.id,
            customerId = this.customerId,
            productId = this.productId,
            category = InsuranceCategory.from(this.category),
            salesChannel = this.salesChannel,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            classification = this.classification?.let { CustomerClassification.valueOf(it) },
            coverages = this.coverages.associate { it.coverageName to it.coverageValue },
            assistances = this.assistances.map { it.assistanceName },
            status = PolicyStatus.valueOf(this.status),
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            history = this.statusHistory.map {
                StatusHistory(
                    status = PolicyStatus.valueOf(it.status),
                    timestamp = it.changedAt
                )
            }.toMutableList()
        )
    }

    fun PolicyRequest.toEntity(): PolicyRequestEntity {
        val entity = PolicyRequestEntity(
            id = this.id,
            customerId = this.customerId,
            productId = this.productId,
            category = this.category.toString(),
            salesChannel = this.salesChannel,
            paymentMethod = this.paymentMethod,
            totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
            insuredAmount = this.insuredAmount,
            status = this.status.toString(),
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            classification = this.classification?.toString()
        )

        this.coverages.forEach { (coverageName, coverageValue) ->
            entity.coverages.add(
                CoverageEntity(
                    id = UUID.randomUUID().toString(),
                    policyRequest = entity,
                    coverageName = coverageName,
                    coverageValue = coverageValue
                )
            )
        }

        this.assistances.forEach { assistanceName ->
            entity.assistances.add(
                AssistanceEntity(
                    id = UUID.randomUUID().toString(),
                    policyRequest = entity,
                    assistanceName = assistanceName
                )
            )
        }

        this.history.forEach { statusHistory ->
            entity.statusHistory.add(
                StatusHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    policyRequest = entity,
                    status = statusHistory.status.name,
                    changedAt = statusHistory.timestamp
                )
            )
        }

        return entity
    }

    fun PolicyRequestEntity.fromDomain(policyRequest: PolicyRequest): PolicyRequestEntity {
        return policyRequest.toEntity()
    }
}