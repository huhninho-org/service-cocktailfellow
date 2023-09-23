package com.cocktailfellow.common

import kotlinx.serialization.Serializable

enum class ErrorType {
    VALIDATION_EXCEPTION, BAD_REQUEST, UNKNOWN_EXCEPTION;

    open fun toLowerCase(): String {
        return name.lowercase().replace('_', '-')
    }
}

@Serializable
data class ErrorResponse(val code: Int, val type: String, val message: String)

open class CustomException(
    override val message: String, val statusCode: HttpStatusCode, val errorType: ErrorType
) : RuntimeException(message)

class ValidationException(
    message: String
) : CustomException(
    message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = ErrorType.VALIDATION_EXCEPTION
)