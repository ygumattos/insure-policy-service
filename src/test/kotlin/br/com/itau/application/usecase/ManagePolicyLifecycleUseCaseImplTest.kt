package br.com.itau.application.usecase

import br.com.itau.application.exceptions.PolicyExceptions.PolicyCreationException
import br.com.itau.application.exceptions.PolicyExceptions.PolicyNotFoundException
import br.com.itau.application.exceptions.PolicyExceptions.PolicyInvalidStateException
import br.com.itau.application.ports.inputs.EvaluatePolicyUseCase
import br.com.itau.application.ports.inputs.PolicyStatusUseCase
import br.com.itau.application.ports.outputs.FraudAnalysis
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.application.ports.outputs.PolicyStatusChangedEventProducer
import br.com.itau.domain.entities.FraudAnalysisResult
import br.com.itau.domain.entities.PolicyRequest
import br.com.itau.domain.enums.CustomerClassification
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.testutils.PolicyFixtures
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ManagePolicyLifecycleUseCaseImplTest {

    private val fraudAnalysis = mockk<FraudAnalysis>()
    private val repository = mockk<PolicyRequestRepository>(relaxed = true)
    private val evaluatePolicyUseCase = mockk<EvaluatePolicyUseCase>()
    private val policyStatusUseCase = mockk<PolicyStatusUseCase>()
    private val eventProducer = mockk<PolicyStatusChangedEventProducer>(relaxed = true)

    private lateinit var useCase: ManagePolicyLifecycleUseCaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = ManagePolicyLifecycleUseCaseImpl(
            fraudAnalysis = fraudAnalysis,
            policyRequestRepository = repository,
            evaluatePolicyUseCase = evaluatePolicyUseCase,
            policyStatusUseCase = policyStatusUseCase,
            policyStatusChangedEventProducer = eventProducer
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(fraudAnalysis, repository, evaluatePolicyUseCase, policyStatusUseCase, eventProducer)
    }

    // -------- create() --------

    @Test
    fun `create - fluxo feliz - valida, move para PENDING e publica evento`() {
        val cmd = PolicyFixtures.command(customerId = "cust-1")

        val created = slot<PolicyRequest>()
        val afterValidation = slot<PolicyRequest>()

        every { repository.save(capture(created)) } answers { created.captured } andThenAnswer { afterValidation.captured }

        every { fraudAnalysis.analyzeFraud(cmd.customerId) } returns FraudAnalysisResult(classification = CustomerClassification.REGULAR)

        every { evaluatePolicyUseCase.execute(any()) } answers {
            (firstArg<PolicyRequest>()).apply { status = PolicyStatus.VALIDATED }
        } andThenAnswer {
            (firstArg<PolicyRequest>())
        }

        every { policyStatusUseCase.moveToPending(any()) } answers {
            val p = firstArg<PolicyRequest>()
            p.status = PolicyStatus.PENDING
            afterValidation.captured = p
            p
        }

        every { eventProducer.publish(any()) } just Runs

        val result = useCase.create(cmd)

        assertThat(result.status).isEqualTo(PolicyStatus.PENDING)

        verifyOrder {
            repository.save(any())
            fraudAnalysis.analyzeFraud(cmd.customerId)
            evaluatePolicyUseCase.execute(any())
            policyStatusUseCase.moveToPending(any())
            repository.save(any())
            eventProducer.publish(any())
        }
    }

    @Test
    fun `create - validacao rejeita - salva e NAO move para pending nem publica`() {
        val cmd = PolicyFixtures.command()
        every { repository.save(any()) } answers { firstArg() } // persist inicial
        every { fraudAnalysis.analyzeFraud(any()) } returns FraudAnalysisResult(classification = CustomerClassification.HIGH_RISK)

        every { evaluatePolicyUseCase.execute(any()) } answers {
            (firstArg<PolicyRequest>()).apply { status = PolicyStatus.REJECTED }
        }

        val result = useCase.create(cmd)

        assertThat(result.status).isEqualTo(PolicyStatus.REJECTED)

        verify(exactly = 2) { repository.save(any()) } // inicial + após validação
        verify(exactly = 0) { policyStatusUseCase.moveToPending(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }

        verify { fraudAnalysis.analyzeFraud(cmd.customerId) }
        verify { evaluatePolicyUseCase.execute(any()) }
    }

    @Test
    fun `create - IllegalArgumentException vira PolicyCreationException`() {
        val cmd = PolicyFixtures.command()
        every { repository.save(any()) } answers { firstArg() }
        every { fraudAnalysis.analyzeFraud(any()) } throws IllegalArgumentException("bad")
        assertThatThrownBy { useCase.create(cmd) }
            .isInstanceOf(PolicyCreationException::class.java)
            .hasMessageContaining("Invalid policy data")

        verify { repository.save(any()) }
        verify(exactly = 1) { fraudAnalysis.analyzeFraud(any()) }
        verify(exactly = 0) { evaluatePolicyUseCase.execute(any()) }
        verify(exactly = 0) { policyStatusUseCase.moveToPending(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }
    }

    @Test
    fun `create - IllegalStateException vira PolicyCreationException`() {
        val cmd = PolicyFixtures.command()
        every { repository.save(any()) } answers { firstArg() }
        every { fraudAnalysis.analyzeFraud(any()) } throws IllegalStateException("rule")
        assertThatThrownBy { useCase.create(cmd) }
            .isInstanceOf(PolicyCreationException::class.java)
            .hasMessageContaining("Policy business rule violation")

        verify { repository.save(any()) }
        verify(exactly = 1) { fraudAnalysis.analyzeFraud(any()) }
        verify(exactly = 0) { evaluatePolicyUseCase.execute(any()) }
        verify(exactly = 0) { policyStatusUseCase.moveToPending(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }
    }

    @Test
    fun `create - erro inesperado vira PolicyCreationException`() {
        val cmd = PolicyFixtures.command()
        every { repository.save(any()) } answers { firstArg() }
        every { fraudAnalysis.analyzeFraud(any()) } throws RuntimeException("boom")

        assertThatThrownBy { useCase.create(cmd) }
            .isInstanceOf(PolicyCreationException::class.java)
            .hasMessageContaining("Unexpected error")

        verify { repository.save(any()) }
        verify(exactly = 1) { fraudAnalysis.analyzeFraud(any()) }
        verify(exactly = 0) { evaluatePolicyUseCase.execute(any()) }
        verify(exactly = 0) { policyStatusUseCase.moveToPending(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }
    }

    // -------- getById() --------

    @Test
    fun `getById - retorna policy`() {
        val policy = PolicyFixtures.newPolicyFrom()
        every { repository.findById(policy.id) } returns policy

        val found = useCase.getById(policy.id)

        assertThat(found.id).isEqualTo(policy.id)
        verify { repository.findById(policy.id) }
    }

    @Test
    fun `getById - not found`() {
        every { repository.findById("missing") } returns null

        assertThatThrownBy { useCase.getById("missing") }
            .isInstanceOf(PolicyNotFoundException::class.java)

        verify { repository.findById("missing") }
    }

    // -------- cancel() --------

    @Test
    fun `cancel - cancela quando pode e publica evento`() {
        val policy = PolicyFixtures.newPolicyFrom()
        // RECEIVED pode cancelar
        every { repository.findById(policy.id) } returns policy

        every { policyStatusUseCase.cancel(policy) } answers {
            policy.status = PolicyStatus.CANCELLED
            policy
        }

        every { repository.save(policy) } returns policy
        every { eventProducer.publish(any()) } just Runs

        useCase.cancel(policy.id)

        verify { repository.findById(policy.id) }
        verify { policyStatusUseCase.cancel(policy) }
        verify { repository.save(policy) }
        verify { eventProducer.publish(any()) }
    }

    @Test
    fun `cancel - not found`() {
        every { repository.findById("missing") } returns null

        assertThatThrownBy { useCase.cancel("missing") }
            .isInstanceOf(PolicyNotFoundException::class.java)

        verify { repository.findById("missing") }
        verify(exactly = 0) { policyStatusUseCase.cancel(any()) }
        verify(exactly = 0) { repository.save(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }
    }

    @Test
    fun `cancel - estado invalido (APPROVED ou REJECTED) lança PolicyInvalidStateException`() {
        val policy = PolicyFixtures.newPolicyFrom()
        policy.status = PolicyStatus.APPROVED
        every { repository.findById(policy.id) } returns policy

        assertThatThrownBy { useCase.cancel(policy.id) }
            .isInstanceOf(PolicyInvalidStateException::class.java)

        verify { repository.findById(policy.id) }
        verify(exactly = 0) { policyStatusUseCase.cancel(any()) }
        verify(exactly = 0) { repository.save(any()) }
        verify(exactly = 0) { eventProducer.publish(any()) }
    }
}
