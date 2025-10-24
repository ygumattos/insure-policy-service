package br.com.itau.adapters.messaging.kafka.producers.dto

import java.math.BigDecimal

data class PolicyStatusChangedEventProducerDTO(
    val id: String,
    val customerId: String,
    val productId: String,
    val category: String,
    val status: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val classification: String
)
