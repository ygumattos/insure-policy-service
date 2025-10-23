package br.com.itau.domain.events

import java.time.Instant

data class PaymentEvent(
    val paymentId: String,
    val policyId: String,
    val status: String,          // e.g. "PAID", "DECLINED"
    val occurredAt: Instant = Instant.now()
)