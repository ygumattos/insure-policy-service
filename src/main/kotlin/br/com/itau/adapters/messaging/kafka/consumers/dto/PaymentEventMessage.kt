package br.com.itau.adapters.messaging.kafka.consumers.dto

data class PaymentEventMessage(
    val id: String,
    val paymentConfirmation: Boolean
)