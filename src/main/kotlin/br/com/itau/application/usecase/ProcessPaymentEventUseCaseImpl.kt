package br.com.itau.application.usecases

import br.com.itau.application.common.logging.Logging
import br.com.itau.application.ports.inputs.ProcessPaymentEventUseCase
import br.com.itau.application.ports.outputs.PolicyRequestRepository
import br.com.itau.domain.enums.PolicyStatus
import br.com.itau.domain.events.ProcessPaymentCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ProcessPaymentEventUseCaseImpl(
    private val repository: PolicyRequestRepository
) : ProcessPaymentEventUseCase, Logging {

    @Transactional
    override fun handle(command: ProcessPaymentCommand) {
        val id = command.policyRequestId

        val snap = repository.findStatusWithHistoryById(id)
        if (snap == null) {
            log.warn("payment-event ignored: policy not found id={}", id)
            return
        }

        if (snap.status != PolicyStatus.PENDING) {
            log.info("payment-event ignored: status must be PENDING, current={} id={}", snap.status, id)
            return
        }

        if (snap.paymentConfirmation != null) {
            log.info("payment-event duplicated: payment already validated (value={}) id={}", snap.paymentConfirmation, id)
            return
        }

        val now = Instant.now()
        val paymentOk = command.paymentConfirmation

        if (!paymentOk) {
            repository.updatePaymentAndStatus(id, false, PolicyStatus.REJECTED, now)
            repository.appendStatusHistory(id, PolicyStatus.REJECTED, now)
            log.info("payment-event applied: REJECTED by payment=false id={}", id)
            return
        }

        if (snap.subscriptionAutorization == true) {
            repository.updatePaymentAndStatus(id, true, PolicyStatus.APPROVED, now)
            repository.appendStatusHistory(id, PolicyStatus.APPROVED, now)
            log.info("payment-event applied: APPROVED (subscription already true) id={}", id)
            return
        }

        repository.updatePaymentOnly(id, true)
        log.info("payment-event applied: payment=true but subscription pending -> status=PENDING id={}", id)
    }
}