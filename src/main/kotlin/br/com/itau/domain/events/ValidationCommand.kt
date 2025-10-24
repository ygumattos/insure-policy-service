package br.com.itau.domain.events

data class ValidationCommand(
    val policyId: String,
    val kind: ValidationKind,
    val value: Boolean
)

enum class ValidationKind { PAYMENT, SUBSCRIPTION }

