package br.com.itau.application.usecase

import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.ProcessValidationEventUseCase
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.domain.events.ValidationCommand
import br.com.itau.domain.events.ValidationKind.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProcessValidationEventUseCaseImpl(
    private val repository: PolicyRequestRepository,
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
        val newStatus =
            when (command.kind) {
                PAYMENT ->
                    if (!command.value) PolicyStatus.REJECTED
                    else if (snapshot.subscriptionAutorization == true) PolicyStatus.APPROVED
                    else PolicyStatus.PENDING
                SUBSCRIPTION ->
                    if (!command.value) PolicyStatus.REJECTED
                    else if (snapshot.paymentConfirmation == true) PolicyStatus.APPROVED
                    else PolicyStatus.PENDING
            }

        val dateFinishAt = if (newStatus == PolicyStatus.PENDING) null else now

        when (command.kind) {
            PAYMENT -> repository.updatePaymentAndStatus(command.policyId, true, newStatus.name, dateFinishAt)
            SUBSCRIPTION ->  repository.updateSubscriptionAndStatus(command.policyId, true, newStatus.name, dateFinishAt)
        }

        repository.appendStatusHistory(command.policyId, newStatus.name, now)
        log.info("status changed id={} -> {}", command.policyId, newStatus)
    }
}