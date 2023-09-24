package com.cocktailfellow

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cocktailfellow.common.CustomException
import com.cocktailfellow.common.ErrorResponse
import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import com.fasterxml.jackson.databind.ObjectMapper


abstract class AbstractRequestHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  abstract fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse

  final override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    return try {
      handleBusinessLogic(input, context)
    } catch (e: Exception) {
      val errorResponse = getErrorResponse(e)
      ApiGatewayResponse.build {
        statusCode = errorResponse.code
        objectBody = objectMapper.writeValueAsString(errorResponse) // serialize the ErrorResponse object
        headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      }
    }
  }

  private fun getErrorResponse(e: Throwable): ErrorResponse {
    return if (e is CustomException) {
      ErrorResponse(
        code = e.statusCode.code,
        type = e.errorType.toLowerCase(),
        message = e.message ?: "An error occurred."
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

