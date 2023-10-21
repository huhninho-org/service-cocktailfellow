package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.mindrot.jbcrypt.BCrypt

class LoginUser : AbstractRequestHandler() {

  private val requiredApiKey: String = System.getenv("APP_API_KEY")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = getBody(input)
    val loginRequest = JsonConfig.instance.decodeFromString<LoginRequest>(body)
    val username = loginRequest.username
    val apiKey = getApiKeyHeader(input)

    if (apiKey == null || apiKey != requiredApiKey) return generateError(HttpStatusCode.FORBIDDEN.code, "Forbidden.")

    val user = UserRepository.getUser(username)

    return if (BCrypt.checkpw(loginRequest.password, user.hashedPassword)) {
      val loginToken = TokenManagement.createLoginToken(username)

      return generateResponse(HttpStatusCode.OK.code, loginToken)
    } else {
      generateError(HttpStatusCode.UNAUTHORIZED.code, "Unauthorized")
    }
  }
}

@Serializable
data class LoginRequest(
  val username: String,
  val password: String
)
