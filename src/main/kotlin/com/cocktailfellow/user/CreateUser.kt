package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.*
import com.cocktailfellow.user.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt

class CreateUser(private val userService: UserService = UserService()) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(CreateUser::class.java)

  private val requiredApiKey: String = System.getenv("APP_API_KEY")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val apiKey = getApiKeyHeader(input)

    val body = getBody(input)
    val user: CreateUserRequest

    if (apiKey == null || apiKey != requiredApiKey) throw JwtTokenException(
      HttpStatusCode.FORBIDDEN.reason,
      ErrorType.JWT_INVALID_EXCEPTION,
      HttpStatusCode.FORBIDDEN
    )

    try {
      user = JsonConfig.instance.decodeFromString(body)
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON body.")
    }

    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

    val createUser = User(
      username = user.username,
      hashedPassword = hashedPassword
    )

    userService.persistUser(createUser)
    log.info("User '${createUser.username}' created.")

    return generateResponse(HttpStatusCode.CREATED.code)
  }
}

@Serializable
data class CreateUserRequest(
  val username: String,
  val password: String
)
