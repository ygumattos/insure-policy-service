package br.com.itau.domain.valueobjects

import java.util.UUID

@JvmInline
value class PolicyRequestId(val value: UUID) {
    companion object {
        fun generate(): PolicyRequestId = PolicyRequestId(UUID.randomUUID())
    }

    override fun toString(): String = value.toString()
}