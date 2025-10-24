package br.com.itau.testutils


import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import br.com.itau.domain.enums.PolicyStatus
import java.math.BigDecimal

object PolicyFixtures {
    fun command(
        customerId: String = "c-1",
        productId: String = "p-1",
        category: InsuranceCategory = InsuranceCategory.OUTROS,
        salesChannel: String = "WEB",
        paymentMethod: String = "CREDIT_CARD",
        totalMonthlyPremiumAmount: BigDecimal = BigDecimal("100.00"),
        insuredAmount: BigDecimal = BigDecimal("1000.00"),
        coverages: Map<String, BigDecimal> = mapOf("BASIC" to BigDecimal("1000.00")),
        assistances: List<String> = listOf("TOW")
    ) = PolicyCommand(
        customerId = customerId,
        productId = productId,
        category = category.name,
        salesChannel = salesChannel,
        paymentMethod = paymentMethod,
        totalMonthlyPremiumAmount = totalMonthlyPremiumAmount,
        insuredAmount = insuredAmount,
        coverages = coverages,
        assistances = assistances
    )

    fun newPolicyFrom(command: PolicyCommand = command()): PolicyRequest =
        PolicyRequest.create(command)

    fun newPolicy(
        status: PolicyStatus = PolicyStatus.RECEIVED,
        category: InsuranceCategory = InsuranceCategory.OUTROS,
        insuredAmount: BigDecimal = BigDecimal("1000.00"),
        classification: CustomerClassification? = null
    ): PolicyRequest {
        val c = command(category = category, insuredAmount = insuredAmount)
        val p = PolicyRequest.create(c)
        if (classification != null) p.classification = classification
        return p
    }
}