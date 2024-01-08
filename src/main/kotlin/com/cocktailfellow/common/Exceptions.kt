package com.cocktailfellow.common

import kotlinx.serialization.Serializable

enum class ErrorType {
  VALIDATION_EXCEPTION,
  BAD_REQUEST,
  RESOURCE_NOT_FOUND,
  CREATE_ITEM_EXCEPTION,
  UPDATE_ITEM_EXCEPTION,
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
data class ErrorResponse(val code: Int, val type: ErrorType, val message: String)

open class CustomException(
  override val message: String, val statusCode: HttpStatusCode, val errorType: ErrorType
) : RuntimeException(message)

class ValidationException(
  message: String
) : CustomException(
  message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = ErrorType.VALIDATION_EXCEPTION
)

class CreateItemException(
  message: String
) : CustomException(
  message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = ErrorType.CREATE_ITEM_EXCEPTION
)

class UpdateItemException(
  message: String
) : CustomException(
  message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = ErrorType.UPDATE_ITEM_EXCEPTION
)

class NotFoundException(
  item: Type
) : CustomException(
  message = "'${item.name}' not found.", statusCode = HttpStatusCode.NOT_FOUND, errorType = ErrorType.RESOURCE_NOT_FOUND
)

class BadRequestException(
  message: String
) : CustomException(
  message = message, statusCode = HttpStatusCode.BAD_REQUEST, errorType = ErrorType.BAD_REQUEST
)

class LinkException(
  message: String,
  statusCode: HttpStatusCode = HttpStatusCode.BAD_REQUEST
) : CustomException(
  message = message, statusCode = statusCode, errorType = ErrorType.LINK_EXCEPTION
)

class JwtTokenException(
  message: String,
  errorType: ErrorType,
  statusCode: HttpStatusCode = HttpStatusCode.UNAUTHORIZED
) : CustomException(
  message = message, statusCode = statusCode, errorType = errorType
)
