package br.com.itau.application.ports.inputs

import br.com.itau.domain.events.ValidationCommand

interface ProcessValidationEventUseCase {
    fun handle(command: ValidationCommand)
}