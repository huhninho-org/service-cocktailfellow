package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.*
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.user.model.User
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt
import javax.validation.constraints.Size

class LoginUser(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userService: UserService = UserService()
) : AbstractRequestHandler() {
  private val requiredApiKey: String = System.getenv("APP_API_KEY")
  private val log: Logger = LogManager.getLogger(LoginUser::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    validateApiKey(getApiKeyHeader(input))
    val loginRequest = ValidationUtil.deserializeAndValidate(getBody(input), LoginRequest::class)
    val username = loginRequest.username

    log.info("User '$username' is trying to log in")
    val user = getUserOrThrow(loginRequest.username)
    validatePassword(loginRequest.password, user.hashedPassword)

    log.info("User '$username' login successful")
    val loginToken = tokenManagement.createLoginToken(loginRequest.username)

    return generateResponse(HttpStatusCode.OK.code, loginToken)
  }

  private fun validateApiKey(apiKey: String?) {
    if (apiKey == null || apiKey != requiredApiKey) {
      throw JwtTokenException(
        HttpStatusCode.FORBIDDEN.reason,
        ErrorType.JWT_INVALID_EXCEPTION,
        HttpStatusCode.FORBIDDEN
      )
    }
  }

  private fun getUserOrThrow(username: String): User {
    return try {
      userService.getUser(username)
    } catch (_: NotFoundException) {
      throw throwJwtTokenException()
    }
  }

  private fun validatePassword(inputPassword: String, storedHashedPassword: String?) {
    if (!BCrypt.checkpw(inputPassword, storedHashedPassword)) {
      throw throwJwtTokenException()
    }
  }
}

private fun throwJwtTokenException(): Throwable {
  throw JwtTokenException(
    "${HttpStatusCode.UNAUTHORIZED.reason}. Invalid credentials.",
    ErrorType.JWT_INVALID_EXCEPTION,
    HttpStatusCode.UNAUTHORIZED
  )
}

@Serializable
data class LoginRequest(
  @field:Size(min = 3, message = "Username must be at least 6 characters.")
  val username: String,
  @field:Size(min = 6, message = "Password must be at least 6 characters.")
  val password: String
)
