package br.com.itau.application.ports.inputs

import br.com.itau.domain.events.ProcessPaymentCommand

interface ProcessPaymentEventUseCase {
    fun handle(command: ProcessPaymentCommand)
}