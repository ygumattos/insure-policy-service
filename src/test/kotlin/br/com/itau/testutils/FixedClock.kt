package br.com.itau.testutils

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

object FixedClock {
    fun at(instant: Instant = Instant.parse("2024-05-10T12:00:00Z")): Clock =
        Clock.fixed(instant, ZoneOffset.UTC)
}