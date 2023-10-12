package com.cocktailfellow

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cocktailfellow.common.CustomException
import com.cocktailfellow.common.ErrorResponse
import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import kotlinx.serialization.Serializable

abstract class AbstractRequestHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  abstract fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse

  final override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    return try {
      handleBusinessLogic(input, context)
    } catch (e: Exception) {
      val errorResponse = getErrorResponse(e)
      generateError(errorResponse.code, errorResponse.message)
    }
  }

  fun generateResponse(status: Int): ApiGatewayResponse {
    return ApiGatewayResponse.withoutBody(status)
  }

  inline fun <reified T> generateResponse(status: Int, result: T, loginToken: String? = null): ApiGatewayResponse {
    val response = ApiResponse(result, loginToken)
    return ApiGatewayResponse.withBody(status, response)
  }

  fun generateError(status: Int, message: String): ApiGatewayResponse {
    return ApiGatewayResponse.error(status, message)
  }

  private fun getErrorResponse(e: Throwable): ErrorResponse {
    return if (e is CustomException) {
      ErrorResponse(
        code = e.statusCode.code,
        type = e.errorType.toLowerCase(),
        message = e.message
      )
    } else {
      ErrorResponse(
        code = HttpStatusCode.INTERNAL_SERVER_ERROR.code,
        type = ErrorType.UNKNOWN_EXCEPTION.toLowerCase(),
        message = e.message ?: "An error occurred."
      )
    }
  }
}

@Serializable
data class ApiResponse<T>(
  val result: T?,
  val loginToken: String?
)
