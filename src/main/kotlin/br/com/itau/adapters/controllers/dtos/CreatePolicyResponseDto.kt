package br.com.itau.adapters.controllers.dtos

import java.time.Instant

data class CreatePolicyResponseDto(
    val id: String,
    val createdAt: Instant
)
