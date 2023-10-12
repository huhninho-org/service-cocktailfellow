package com.cocktailfellow

import com.cocktailfellow.common.JsonConfig
import kotlinx.serialization.encodeToString
import java.util.*

class ApiGatewayResponse(
  val statusCode: Int = 200,
  var body: String? = null,
  val headers: Map<String, String>? = Collections.emptyMap(),
  val isBase64Encoded: Boolean = false
) {
  companion object {
    val defaultHeaders = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")

    inline fun <reified T> withBody(status: Int, bodyObject: T): ApiGatewayResponse {
      val body = JsonConfig.instance.encodeToString(bodyObject)
      return ApiGatewayResponse(status, body, defaultHeaders)
    }

    fun withoutBody(status: Int): ApiGatewayResponse {
      return ApiGatewayResponse(status, body = null, defaultHeaders)
    }

    fun error(status: Int, message: String): ApiGatewayResponse {
      val body = JsonConfig.instance.encodeToString(mapOf("error" to message))
      return ApiGatewayResponse(status, body, defaultHeaders)
    }
  }
}
