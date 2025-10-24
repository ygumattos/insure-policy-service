package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyEvaluationConfig
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.InsuranceCategory
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EvaluatePolicyUseCaseImplTest {

    private val policyStatusUseCase = mockk<PolicyStatusUseCase>(relaxed = true)

    private val config = PolicyEvaluationConfig(
        regular = PolicyEvaluationConfig.LimitConfig(
            vidaResidencial = bd("500000"),
            auto = bd("350000"),
            outros = bd("255000")
        ),
        highRisk = PolicyEvaluationConfig.LimitConfig(
            auto = bd("250000"),
            residencial = bd("150000"),
            outros = bd("125000")
        ),
        preferential = PolicyEvaluationConfig.LimitConfig(
            vida = bd("800000"),
            autoResidencial = bd("450000"),
            outros = bd("375000")
        ),
        noInfo = PolicyEvaluationConfig.LimitConfig(
            vidaResidencial = bd("200000"),
            auto = bd("75000"),
            outros = bd("55000")
        )
    )

    private lateinit var useCase: EvaluatePolicyUseCaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = EvaluatePolicyUseCaseImpl(config, policyStatusUseCase)

        // por padrão, validate/reject retornam o próprio objeto recebido
        every { policyStatusUseCase.validate(any()) } answers { firstArg<PolicyRequest>() }
        every { policyStatusUseCase.reject(any()) } answers { firstArg<PolicyRequest>() }
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(policyStatusUseCase)
    }

    // Helpers -----------------------------------------------------------------------------------

    private fun bd(v: String) = BigDecimal(v)

    private fun newPolicy(
        category: InsuranceCategory,
        insured: String,
        classification: CustomerClassification?
    ): PolicyRequest {
        val cmd = PolicyCommand(
            customerId = "c1",
            productId = "p1",
            category = category.name,
            salesChannel = "WEB",
            paymentMethod = "CARD",
            totalMonthlyPremiumAmount = bd("100.00"),
            insuredAmount = bd(insured),
            coverages = mapOf("BASIC" to bd("1000")),
            assistances = listOf("ASSIST")
        )
        return PolicyRequest.create(cmd).apply {
            this.classification = classification
        }
    }

    // ----------------------------------- REGULAR -----------------------------------------------

    @Nested
    inner class RegularTests {
        @Test
        fun `REGULAR - VIDA e RESIDENCIAL - limite inclusivo (== aprova)`() {
            val pVida = newPolicy(InsuranceCategory.VIDA, "500000", CustomerClassification.REGULAR)
            val pRes = newPolicy(InsuranceCategory.RESIDENCIAL, "500000", CustomerClassification.REGULAR)

            useCase.execute(pVida)
            useCase.execute(pRes)

            verify { policyStatusUseCase.validate(pVida) }
            verify { policyStatusUseCase.validate(pRes) }
        }

        @Test
        fun `REGULAR - AUTO - acima do limite rejeita`() {
            val p = newPolicy(InsuranceCategory.AUTO, "350001", CustomerClassification.REGULAR)

            useCase.execute(p)

            verify { policyStatusUseCase.reject(p) }
        }

        @Test
        fun `REGULAR - OUTROS - limite inclusivo (== aprova)`() {
            val p = newPolicy(InsuranceCategory.OUTROS, "255000", CustomerClassification.REGULAR)

            useCase.execute(p)

            verify { policyStatusUseCase.validate(p) }
        }
    }

    // ----------------------------------- HIGH RISK ---------------------------------------------

    @Nested
    inner class HighRiskTests {
        @Test
        fun `HIGH_RISK - RESIDENCIAL - limite inclusivo (== aprova)`() {
            val p = newPolicy(InsuranceCategory.RESIDENCIAL, "150000", CustomerClassification.HIGH_RISK)

            useCase.execute(p)

            verify { policyStatusUseCase.validate(p) }
        }

        @Test
        fun `HIGH_RISK - AUTO - acima do limite rejeita`() {
            val p = newPolicy(InsuranceCategory.AUTO, "250001", CustomerClassification.HIGH_RISK)

            useCase.execute(p)

            verify { policyStatusUseCase.reject(p) }
        }

        @Test
        fun `HIGH_RISK - OUTROS - limite inclusivo (== aprova)`() {
            val p = newPolicy(InsuranceCategory.OUTROS, "125000", CustomerClassification.HIGH_RISK)

            useCase.execute(p)

            verify { policyStatusUseCase.validate(p) }
        }
    }

    // ----------------------------------- PREFERENTIAL ------------------------------------------

    @Nested
    inner class PreferentialTests {
        @Test
        fun `PREFERENTIAL - VIDA - limite exclusivo (== rejeita, menor aprova)`() {
            val equal = newPolicy(InsuranceCategory.VIDA, "800000", CustomerClassification.PREFERENTIAL)
            val below = newPolicy(InsuranceCategory.VIDA, "799999", CustomerClassification.PREFERENTIAL)

            useCase.execute(equal)
            useCase.execute(below)

            verify { policyStatusUseCase.reject(equal) }   // exclusivo
            verify { policyStatusUseCase.validate(below) } // menor que limite
        }

        @Test
        fun `PREFERENTIAL - AUTO e RESIDENCIAL - limite exclusivo (== rejeita)`() {
            val autoEq = newPolicy(InsuranceCategory.AUTO, "450000", CustomerClassification.PREFERENTIAL)
            val resEq = newPolicy(InsuranceCategory.RESIDENCIAL, "450000", CustomerClassification.PREFERENTIAL)
            val autoBelow = newPolicy(InsuranceCategory.AUTO, "449999", CustomerClassification.PREFERENTIAL)
            val resBelow = newPolicy(InsuranceCategory.RESIDENCIAL, "449999", CustomerClassification.PREFERENTIAL)

            useCase.execute(autoEq)
            useCase.execute(resEq)
            useCase.execute(autoBelow)
            useCase.execute(resBelow)

            verify { policyStatusUseCase.reject(autoEq) }
            verify { policyStatusUseCase.reject(resEq) }
            verify { policyStatusUseCase.validate(autoBelow) }
            verify { policyStatusUseCase.validate(resBelow) }
        }

        @Test
        fun `PREFERENTIAL - OUTROS - limite inclusivo (== aprova)`() {
            val pEq = newPolicy(InsuranceCategory.OUTROS, "375000", CustomerClassification.PREFERENTIAL)
            val pAbove = newPolicy(InsuranceCategory.OUTROS, "375001", CustomerClassification.PREFERENTIAL)

            useCase.execute(pEq)
            useCase.execute(pAbove)

            verify { policyStatusUseCase.validate(pEq) } // inclusivo
            verify { policyStatusUseCase.reject(pAbove) }
        }
    }

    // ----------------------------------- NO INFORMATION ----------------------------------------

    @Nested
    inner class NoInfoTests {
        @Test
        fun `NO_INFO - VIDA e RESIDENCIAL - limite inclusivo (== aprova)`() {
            val vidaEq = newPolicy(InsuranceCategory.VIDA, "200000", CustomerClassification.NO_INFORMATION)
            val resEq = newPolicy(InsuranceCategory.RESIDENCIAL, "200000", CustomerClassification.NO_INFORMATION)

            useCase.execute(vidaEq)
            useCase.execute(resEq)

            verify { policyStatusUseCase.validate(vidaEq) }
            verify { policyStatusUseCase.validate(resEq) }
        }

        @Test
        fun `NO_INFO - AUTO - limite inclusivo (== aprova)`() {
            val p = newPolicy(InsuranceCategory.AUTO, "75000", CustomerClassification.NO_INFORMATION)

            useCase.execute(p)

            verify { policyStatusUseCase.validate(p) }
        }

        @Test
        fun `NO_INFO - OUTROS - acima rejeita`() {
            val p = newPolicy(InsuranceCategory.OUTROS, "55001", CustomerClassification.NO_INFORMATION)

            useCase.execute(p)

            verify { policyStatusUseCase.reject(p) }
        }
    }

    // ----------------------------------- Null classification -----------------------------------

    @Test
    fun `classification null - sempre rejeita`() {
        val p = newPolicy(InsuranceCategory.VIDA, "1", null)

        useCase.execute(p)

        verify { policyStatusUseCase.reject(p) }
    }
}
