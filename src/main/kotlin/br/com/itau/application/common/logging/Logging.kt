package br.com.itau.application.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Logging {
    val log: Logger get() = LoggerFactory.getLogger(javaClass)
}