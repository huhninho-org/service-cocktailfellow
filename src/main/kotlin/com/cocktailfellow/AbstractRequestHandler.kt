package com.cocktailfellow

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cocktailfellow.common.*
import kotlinx.serialization.Serializable

abstract class AbstractRequestHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  abstract fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse

  final override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    return try {
      handleBusinessLogic(input, context)
    } catch (e: Exception) {
      val errorResponse = getErrorResponse(e)
      generateError(errorResponse.code, errorResponse.type, errorResponse.message)
    }
  }

  fun generateResponse(status: Int): ApiGatewayResponse {
    return ApiGatewayResponse.withoutBody(status)
  }

  fun generateResponse(status: Int, loginToken: String): ApiGatewayResponse {
    val loginResponse = LoginResponse(
      loginToken = loginToken
    )
    return ApiGatewayResponse.withBody(status, loginResponse)
  }

  inline fun <reified T> generateResponse(status: Int, result: T, loginToken: String? = null): ApiGatewayResponse {
    val response = ApiResponse(result, loginToken)
    return ApiGatewayResponse.withBody(status, response)
  }

  private fun generateError(status: Int, type: ErrorType, message: String): ApiGatewayResponse {
    return ApiGatewayResponse.error(status, type, message)
  }

  private fun getErrorResponse(e: Throwable): ErrorResponse {
    return if (e is CustomException) {
      ErrorResponse(
        code = e.statusCode.code,
        type = e.errorType,
        message = e.message
      )
    } else {
      ErrorResponse(
        code = HttpStatusCode.INTERNAL_SERVER_ERROR.code,
        type = ErrorType.UNKNOWN_EXCEPTION,
        message = e.message ?: "An error occurred."
      )
    }
  }

  private fun getInputHeaders(input: Map<String, Any>): Map<*, *>? {
    return input["headers"] as? Map<*, *>
  }

  private fun getPathParameters(input: Map<String, Any>): Map<*, *>? {
    return input["pathParameters"] as? Map<*, *>
  }

  protected fun getAuthorizationHeader(input: Map<String, Any>): String? {
    val headers = getInputHeaders(input)
    val authorizationHeader = headers?.keys
        ?.filterIsInstance<String>()
        ?.firstOrNull { it.equals("Authorization", ignoreCase = true) }
    return authorizationHeader?.let { headers[it] as? String }
  }

  protected fun getApiKeyHeader(input: Map<String, Any>): String? {
    return getInputHeaders(input)?.get("x-api-key") as? String
  }

  protected fun getPathParameterGroupId(input: Map<String, Any>): String {
    return getPathParameters(input)?.get("groupId") as? String
      ?: throw ValidationException("Invalid group ID.")
  }

  protected fun getPathParameterCocktailId(input: Map<String, Any>): String {
    return getPathParameters(input)?.get("cocktailId") as? String
      ?: throw ValidationException("Invalid cocktail ID.")
  }

  protected fun getQueryParameterIngredients(input: Map<String, Any>): List<String> {
    val queryStringParameters =
      input["queryStringParameters"] as? Map<String, String> ?: throw ValidationException("Missing query parameters.")
    val ingredients = queryStringParameters["ingredients"]
    if (ingredients.isNullOrEmpty())
      throw ValidationException("Missing ingredients parameter.")
    return ingredients.split(",")
  }

  protected fun getBody(input: Map<String, Any>): String {
    val body: String? = input["body"] as? String
    if (body.isNullOrEmpty() || body == "{}")
      throw ValidationException("Missing body parameter.")
    return body
  }
}

@Serializable
data class ApiResponse<T>(
  val result: T?,
  val loginToken: String?
)

@Serializable
data class LoginResponse(
  val loginToken: String
)
