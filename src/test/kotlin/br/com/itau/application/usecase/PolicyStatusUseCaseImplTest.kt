package br.com.itau.application.usecase

import br.com.itau.domain.entities.PolicyCommand
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.InsuranceCategory
import br.com.itau.domain.enums.PolicyStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PolicyStatusUseCaseImplTest {

    private val useCase = PolicyStatusUseCaseImpl()

    private fun command(
        category: InsuranceCategory = InsuranceCategory.OUTROS,
        insured: String = "1000.00",
        premium: String = "100.00",
    ) = PolicyCommand(
        customerId = "c-1",
        productId = "p-1",
        category = category.name,
        salesChannel = "WEB",
        paymentMethod = "CARD",
        totalMonthlyPremiumAmount = BigDecimal(premium),
        insuredAmount = BigDecimal(insured),
        coverages = mapOf("BASIC" to BigDecimal("1000.00")),
        assistances = listOf("ASSIST-1")
    )

    private fun newPolicy(): PolicyRequest = PolicyRequest.create(command())

    @Nested
    inner class ValidateTests {
        @Test
        fun `validate - RECEIVED - OK`() {
            val p = newPolicy().apply { status = PolicyStatus.RECEIVED }

            val updated = useCase.validate(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.VALIDATED)
            assertThat(updated.history).isNotEmpty
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.VALIDATED)
            assertThat(updated.finishedAt).isNull()
        }

        @Test
        fun `validate - nao RECEIVED - erro`() {
            val p = newPolicy().apply { status = PolicyStatus.PENDING }

            assertThatThrownBy { useCase.validate(p) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Only RECEIVED policies can be validated")
        }
    }

    @Nested
    inner class PendingTests {
        @Test
        fun `moveToPending - VALIDATED - OK`() {
            val p = newPolicy().apply { status = PolicyStatus.VALIDATED }

            val updated = useCase.moveToPending(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.PENDING)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.PENDING)
            assertThat(updated.finishedAt).isNull()
        }

        @Test
        fun `moveToPending - nao VALIDATED - erro`() {
            val p = newPolicy().apply { status = PolicyStatus.RECEIVED }

            assertThatThrownBy { useCase.moveToPending(p) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Only VALIDATED policies can move to PENDING")
        }
    }

    @Nested
    inner class ApproveTests {
        @Test
        fun `approve - PENDING - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.PENDING }

            val updated = useCase.approve(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.APPROVED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.APPROVED)
            assertThat(updated.finishedAt).isNotNull() // finaliza
        }

        @Test
        fun `approve - nao PENDING - erro`() {
            val p = newPolicy().apply { status = PolicyStatus.VALIDATED }

            assertThatThrownBy { useCase.approve(p) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Only PENDING policies can be APPROVED")
        }
    }

    @Nested
    inner class RejectTests {
        @Test
        fun `reject - RECEIVED - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.RECEIVED }

            val updated = useCase.reject(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `reject - VALIDATED - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.VALIDATED }

            val updated = useCase.reject(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `reject - PENDING - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.PENDING }

            val updated = useCase.reject(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.REJECTED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `reject - status invalido - erro`() {
            val p = newPolicy().apply { status = PolicyStatus.APPROVED }

            assertThatThrownBy { useCase.reject(p) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Cannot reject policy with status")
        }
    }

    @Nested
    inner class CancelTests {
        @Test
        fun `cancel - RECEIVED - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.RECEIVED }

            val updated = useCase.cancel(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `cancel - VALIDATED - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.VALIDATED }

            val updated = useCase.cancel(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `cancel - PENDING - OK e finaliza`() {
            val p = newPolicy().apply { status = PolicyStatus.PENDING }

            val updated = useCase.cancel(p)

            assertThat(updated.status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.history.last().status).isEqualTo(PolicyStatus.CANCELLED)
            assertThat(updated.finishedAt).isNotNull()
        }

        @Test
        fun `cancel - status proibido - erro`() {
            listOf(PolicyStatus.APPROVED, PolicyStatus.REJECTED, PolicyStatus.CANCELLED).forEach { st ->
                val p = newPolicy().apply { status = st }

                assertThatThrownBy { useCase.cancel(p) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("Cannot cancel policy with status")
            }
        }
    }
}
