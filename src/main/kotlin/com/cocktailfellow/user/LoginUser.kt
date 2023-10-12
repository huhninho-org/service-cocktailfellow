package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.model.LoginRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.mindrot.jbcrypt.BCrypt

class LoginUser : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = input["body"] as String
    val loginRequest = JsonConfig.instance.decodeFromString<LoginRequest>(body)
    val username = loginRequest.username

    val user = UserRepository.getUser(username)

    return if (BCrypt.checkpw(loginRequest.password, user.hashedPassword)) {
      val loginToken = TokenManagement.createLoginToken(username)
      val refreshToken = TokenManagement.createRefreshToken(username)

      val loginResponse = LoginResponse(
        loginToken = loginToken,
        refreshToken = refreshToken
      )

      return generateLoginResponse(HttpStatusCode.OK.code, loginResponse)
    } else {
      generateError(HttpStatusCode.UNAUTHORIZED.code, "Unauthorized")
    }
  }

  private fun generateLoginResponse(status: Int, result: LoginResponse): ApiGatewayResponse {
    val response = CustomApiResponse(result)
    return ApiGatewayResponse.withBody(status, response)
  }
}

@Serializable
data class CustomApiResponse<LoginResponse>(
  val result: LoginResponse,
)

@Serializable
data class LoginResponse(
  val loginToken: String?,
  val refreshToken: String?
)
