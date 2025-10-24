package br.com.itau.application.usecase

import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.ProcessValidationEventUseCase
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.application.ports.outputs.PolicyStatusChangedEventProducer
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.domain.events.ValidationCommand
import br.com.itau.domain.events.ValidationKind.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProcessValidationEventUseCaseImpl(
    private val repository: PolicyRequestRepository,
    private val policyStatusChangedEventProducer: PolicyStatusChangedEventProducer
): Logging, ProcessValidationEventUseCase {

    override fun handle(command: ValidationCommand) {
        val snapshot = repository.findStatusWithHistoryById(command.policyId)
            ?: run { log.warn("policy not found id={}", command.policyId); return }

        if (snapshot.status != PolicyStatus.PENDING) {
            log.info("ignore: status={} id={}", snapshot.status, command.policyId); return
        }

        when (command.kind) {
            PAYMENT -> if (snapshot.paymentConfirmation != null) {
                log.info("duplicate payment id={}", command.policyId); return
            }
            SUBSCRIPTION -> if (snapshot.subscriptionAutorization != null) {
                log.info("duplicate subscription id={}", command.policyId); return
            }
        }

        val now = Instant.now()
        val snapshotUpdated =
            when (command.kind) {
                PAYMENT ->
                    if (!command.value) snapshot.copy(status = PolicyStatus.REJECTED, paymentConfirmation = false, finishedAt = now)
                    else if (snapshot.subscriptionAutorization == true) snapshot.copy(status = PolicyStatus.APPROVED, paymentConfirmation = true, finishedAt = now)
                    else snapshot.copy(status = PolicyStatus.PENDING, paymentConfirmation = true, finishedAt = null)
                SUBSCRIPTION ->
                    if (!command.value) snapshot.copy(status = PolicyStatus.REJECTED, subscriptionAutorization = false, finishedAt = now)
                    else if (snapshot.paymentConfirmation == true) snapshot.copy(status = PolicyStatus.APPROVED, subscriptionAutorization = true, finishedAt = now)
                    else snapshot.copy(status = PolicyStatus.PENDING, subscriptionAutorization = true, finishedAt = null)
            }

        when (command.kind) {
            PAYMENT -> repository.updatePaymentAndStatus(
                command.policyId,
                snapshotUpdated.paymentConfirmation,
                snapshotUpdated.status.name,
                snapshotUpdated.finishedAt
            )
            SUBSCRIPTION -> repository.updateSubscriptionAndStatus(
                command.policyId,
                snapshotUpdated.subscriptionAutorization,
                snapshotUpdated.status.name,
                snapshotUpdated.finishedAt
            )

        }

        repository.appendStatusHistory(command.policyId, snapshotUpdated.status.name, now)
        log.info("status changed id={} -> {}", command.policyId, snapshotUpdated.status)

        policyStatusChangedEventProducer.publish(snapshotUpdated)
        log.info("Policy send to StatusChanged queue successfully. ID: {}", snapshot.id)
    }
}