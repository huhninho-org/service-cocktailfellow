package com.cocktailfellow.common

import kotlinx.serialization.Serializable

enum class ErrorType {
  VALIDATION_EXCEPTION,
  BAD_REQUEST,
  JWT_EXPIRED_EXCEPTION,
  JWT_INVALID_SIGNATURE_EXCEPTION,
  JWT_INVALID_EXCEPTION,
  LINK_EXCEPTION,

  UNKNOWN_EXCEPTION;

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

class LinkException(
  message: String,
  statusCode: HttpStatusCode = HttpStatusCode.BAD_REQUEST
) : CustomException(
  message = message, statusCode = statusCode, errorType = ErrorType.LINK_EXCEPTION
)

class JwtTokenException(
  message: String,
  errorType: ErrorType
) : CustomException(
  message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = errorType
)
