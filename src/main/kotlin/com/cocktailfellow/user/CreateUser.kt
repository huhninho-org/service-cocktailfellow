package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.model.UserCreate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class CreateUser : AbstractRequestHandler() {

  private val log: Logger = LogManager.getLogger(CreateUser::class.java)

  private val requiredApiKey: String = System.getenv("APP_API_KEY")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val apiKey = getApiKeyHeader(input)

    val body = getBody(input)
    val user: CreateUserRequest

    if (apiKey == null || apiKey != requiredApiKey) return generateError(HttpStatusCode.FORBIDDEN.code, "Forbidden.")

    try {
      user = body.let { JsonConfig.instance.decodeFromString(it) }
        ?: throw ValidationException("No user found in the request body.")
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON")
    }

    val userId = UUID.randomUUID().toString()
    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

    val createUser = UserCreate(
      userId = userId,
      username = user.username,
      hashedPassword = hashedPassword
    )

    UserRepository.persistUser(createUser)
    log.info("User '${createUser.username}' created.")

    return generateResponse(HttpStatusCode.CREATED.code)
  }
}

@Serializable
data class CreateUserRequest(
  val username: String,
  val password: String
)
