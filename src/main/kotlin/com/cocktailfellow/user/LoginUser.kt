package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.JwtTokenException
import com.cocktailfellow.common.token.TokenManagement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt

class LoginUser(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userService: UserService = UserService()
) : AbstractRequestHandler() {
  private val requiredApiKey: String = System.getenv("APP_API_KEY")
  private val log: Logger = LogManager.getLogger(LoginUser::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = getBody(input)
    val loginRequest = JsonConfig.instance.decodeFromString<LoginRequest>(body)
    val username = loginRequest.username
    val apiKey = getApiKeyHeader(input)

    if (apiKey == null || apiKey != requiredApiKey) throw JwtTokenException(
      HttpStatusCode.FORBIDDEN.reason,
      ErrorType.JWT_INVALID_EXCEPTION,
      HttpStatusCode.FORBIDDEN
    )

    log.info("User '$username' is trying to log in")
    val user = userService.getUser(username)
    log.info("User '$username' login successful")

    if (BCrypt.checkpw(loginRequest.password, user.hashedPassword)) {
      val loginToken = tokenManagement.createLoginToken(username)
      return generateResponse(HttpStatusCode.OK.code, loginToken)
    } else {
      throw JwtTokenException(
        HttpStatusCode.UNAUTHORIZED.reason,
        ErrorType.JWT_INVALID_EXCEPTION,
        HttpStatusCode.UNAUTHORIZED
      )
    }
  }
}

@Serializable
data class LoginRequest(
  val username: String,
  val password: String
)
