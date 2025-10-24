package br.com.itau.application.usecase

import br.com.itau.application.ports.inputs.ProcessValidationEventUseCase
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.application.ports.outputs.PolicyStatusChangedEventProducer
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.entities.StatusHistory
import br.com.itau.domain.events.ValidationCommand
import br.com.itau.domain.events.ValidationKind
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ProcessValidationEventUseCaseImplTest {

    private val repository = mockk<PolicyRequestRepository>(relaxed = true)
    private val policyStatusChangedEventProducer = mockk<PolicyStatusChangedEventProducer>()
    private val fixedNow = Instant.parse("2024-05-10T12:00:00Z")

    private lateinit var useCase: ProcessValidationEventUseCase

    @BeforeEach
    fun setup() {
        clearAllMocks()
        useCase = ProcessValidationEventUseCaseImpl(repository, policyStatusChangedEventProducer)
    }

    private fun snapshot(
        id: String = "policy-1",
        status: PolicyStatus = PolicyStatus.PENDING,
        payment: Boolean? = null,
        subscription: Boolean? = null
    ) = PolicyRequest.PolicyStatusSnapshot(
        id = id,
        status = status,
        paymentConfirmation = payment,
        subscriptionAutorization = subscription,
        finishedAt = null,
        history = emptyList<StatusHistory>().toMutableList()
    )

    @Test
    fun `ignora quando status nao e PENDING`() {
        every { repository.findStatusWithHistoryById("id-1") } returns snapshot(id = "id-1", status = PolicyStatus.APPROVED)

        val cmd = ValidationCommand("id-1", ValidationKind.PAYMENT, value = true)
        useCase.handle(cmd)

        verify(exactly = 1) { repository.findStatusWithHistoryById("id-1") }
        verify(exactly = 0) { repository.updatePaymentAndStatus(any(), any(), any(), any()) }
        verify(exactly = 0) { repository.appendStatusHistory(any(), any(), any()) }
    }

    @Test
    fun `ignora payment duplicado`() {
        every { repository.findStatusWithHistoryById("id-2") } returns snapshot(id = "id-2", status = PolicyStatus.PENDING, payment = true)

        useCase.handle(ValidationCommand("id-2", ValidationKind.PAYMENT, value = true))

        verify(exactly = 1) { repository.findStatusWithHistoryById(any()) }
    }

    @Test
    fun `ignora subscription duplicada`() {
        every { repository.findStatusWithHistoryById("id-3") } returns snapshot(id = "id-3", status = PolicyStatus.PENDING, subscription = false)

        useCase.handle(ValidationCommand("id-3", ValidationKind.SUBSCRIPTION, value = false))

        verify(exactly = 1) { repository.findStatusWithHistoryById(any()) }
    }

    @Test
    fun `payment false leva a REJECTED e registra historico`() {
        every { repository.findStatusWithHistoryById("id-4") } returns snapshot(id = "id-4", status = PolicyStatus.PENDING, subscription = null)
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-4", ValidationKind.PAYMENT, value = false))

        verify { repository.updatePaymentAndStatus("id-4", false, PolicyStatus.REJECTED.name, any()) }
        verify { repository.appendStatusHistory("id-4", PolicyStatus.REJECTED.name, any()) }
    }

    @Test
    fun `payment true com subscription true leva a APPROVED`() {
        every { repository.findStatusWithHistoryById("id-5") } returns snapshot(id = "id-5", status = PolicyStatus.PENDING, subscription = true)
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-5", ValidationKind.PAYMENT, value = true))

        verify { repository.updatePaymentAndStatus("id-5", true, PolicyStatus.APPROVED.name, any()) }
        verify { repository.appendStatusHistory("id-5", PolicyStatus.APPROVED.name, any()) }
    }

    @Test
    fun `payment true com subscription null apenas seta flag`() {
        every { repository.findStatusWithHistoryById("id-6") } returns snapshot(id = "id-6", status = PolicyStatus.PENDING, subscription = null)
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-6", ValidationKind.PAYMENT, value = true))
        every { repository.appendStatusHistory(any(), any(), any()) } returns Unit

        verify(exactly = 1) { repository.findStatusWithHistoryById(any()) }
        verify(exactly = 1) { repository.appendStatusHistory(any(), any(), any()) }
    }

    @Test
    fun `subscription false leva a REJECTED`() {
        every { repository.findStatusWithHistoryById("id-7") } returns snapshot(id = "id-7", status = PolicyStatus.PENDING, payment = null)
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-7", ValidationKind.SUBSCRIPTION, value = false))

        verify { repository.updateSubscriptionAndStatus("id-7", false, PolicyStatus.REJECTED.name, any()) }
        verify { repository.appendStatusHistory("id-7", PolicyStatus.REJECTED.name, any()) }
    }

    @Test
    fun `subscription true com payment true leva a APPROVED`() {
        every { repository.findStatusWithHistoryById("id-8") } returns snapshot(id = "id-8", status = PolicyStatus.PENDING, payment = true)
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-8", ValidationKind.SUBSCRIPTION, value = true))

        verify { repository.updateSubscriptionAndStatus("id-8", true, PolicyStatus.APPROVED.name, any()) }
        verify { repository.appendStatusHistory("id-8", PolicyStatus.APPROVED.name, any()) }
    }

    @Test
    fun `subscription true com payment null apenas seta flag`() {
        every { repository.findStatusWithHistoryById("id-9") } returns snapshot(id = "id-9", status = PolicyStatus.PENDING, payment = null)
        every { repository.appendStatusHistory(any(), any(), any()) } returns Unit
        every { policyStatusChangedEventProducer.publish(any()) } returns Unit
        useCase.handle(ValidationCommand("id-9", ValidationKind.SUBSCRIPTION, value = true))

        verify(exactly = 1) { repository.findStatusWithHistoryById(any()) }
        verify(exactly = 1) { repository.appendStatusHistory(any(), any(), any()) }
    }
}
