package com.example.domain.model

/**
 * Sealed hierarchy of domain-level errors providing structured categorization and user-facing messages.
 */
sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class Network(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Parse(
        override val message: String,
        val lineNumber: Int? = null,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Storage(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Playback(
        override val message: String,
        val errorCode: Int? = null,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Validation(
        override val message: String
    ) : AppError(message)

    data class NotFound(
        override val message: String
    ) : AppError(message)

    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}
