package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JwtTokenException
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.CREDENTIALS_MAX
import com.cocktailfellow.common.validation.Validation.PASSWORD_MIN
import com.cocktailfellow.common.validation.Validation.USERNAME_MIN
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
    val loginRequest = Validation.deserializeAndValidate(getBody(input), LoginRequest::class)
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
  private fun throwJwtTokenException(): Throwable {
    log.error("Invalid credentials.")
    throw JwtTokenException(
      "${HttpStatusCode.UNAUTHORIZED.reason}. Invalid credentials.",
      ErrorType.JWT_INVALID_EXCEPTION,
      HttpStatusCode.UNAUTHORIZED
    )
  }
}

@Serializable
data class LoginRequest(
  @field:Size(
    min = USERNAME_MIN,
    max = CREDENTIALS_MAX,
    message = "'username' length should be within $USERNAME_MIN to $CREDENTIALS_MAX characters."
  )
  val username: String,
  @field:Size(
    min = PASSWORD_MIN,
    max = CREDENTIALS_MAX,
    message = "'password' length should be within $PASSWORD_MIN to $CREDENTIALS_MAX characters."
  )
  val password: String
)
