package br.com.itau.application.exceptions

import br.com.itau.application.common.logging.Logging
import br.com.itau.application.exceptions.PolicyExceptions.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler : Logging {

    @ExceptionHandler(PolicyNotFoundException::class)
    fun handlePolicyNotFoundException(e: PolicyNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("Policy not found: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("NOT_FOUND", e.message ?: "Policy not found"))
    }

    @ExceptionHandler(PolicyCreationException::class)
    fun handlePolicyCreationException(e: PolicyCreationException): ResponseEntity<ErrorResponse> {
        log.error("Policy creation failed: {}", e.message, e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("POLICY_CREATION_ERROR", e.message ?: "Policy creation failed"))
    }

    @ExceptionHandler(PolicyRetrievalException::class)
    fun handlePolicyRetrievalException(e: PolicyRetrievalException): ResponseEntity<ErrorResponse> {
        log.error("Policy retrieval failed: {}", e.message, e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("POLICY_RETRIEVAL_ERROR", e.message ?: "Policy retrieval failed"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error: {}", e.message, e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
    }

    data class ErrorResponse(
        val code: String,
        val message: String,
        val timestamp: String = java.time.Instant.now().toString()
    )
}