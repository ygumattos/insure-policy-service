package br.com.itau.application.exceptions

class PolicyExceptions {
    open class PolicyException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
    class PolicyCreationException(message: String, cause: Throwable? = null) : PolicyException(message, cause)
    class PolicyNotFoundException(message: String, cause: Throwable? = null) : PolicyException(message, cause)
    class PolicyRetrievalException(message: String, cause: Throwable? = null) : PolicyException(message, cause)
    class PolicyInvalidStateException(message: String, cause: Throwable? = null) : PolicyException(message, cause)

}