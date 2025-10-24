package br.com.itau.adapters.messaging.kafka.consumers.dto

data class SubscriptionEventMessage(
    val id: String,
    val subscriptionAuthorization: Boolean
)
