package com.cocktailfellow

import com.cocktailfellow.common.JsonConfig
import kotlinx.serialization.encodeToString

class ApiGatewayResponse(
  val statusCode: Int = 200,
  var body: String? = null,
  val headers: Map<String, String>? = mapOf(
    "X-Powered-By" to "AWS Lambda & serverless",
    "Content-Type" to "application/json"
  )
) {
  companion object {

    inline fun <reified T> withBody(status: Int, bodyObject: T): ApiGatewayResponse {
      val body = JsonConfig.instance.encodeToString(bodyObject)
      return ApiGatewayResponse(statusCode = status, body = body)
    }

    fun withoutBody(status: Int): ApiGatewayResponse {
      return ApiGatewayResponse(statusCode = status)
    }

    fun error(status: Int, type: String, message: String): ApiGatewayResponse {
      val body = JsonConfig.instance.encodeToString(mapOf("type" to type, "error" to message))
      return ApiGatewayResponse(statusCode = status, body = body)
    }
  }
}
