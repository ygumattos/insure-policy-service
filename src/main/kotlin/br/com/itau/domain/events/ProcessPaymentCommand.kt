package br.com.itau.domain.events

data class ProcessPaymentCommand(
    val policyRequestId: String,
    val paymentConfirmation: Boolean
)