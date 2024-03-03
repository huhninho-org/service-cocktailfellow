package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JwtTokenException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.CREDENTIALS_MAX
import com.cocktailfellow.common.validation.Validation.PASSWORD_MIN
import com.cocktailfellow.common.validation.Validation.USERNAME_MIN
import com.cocktailfellow.user.model.User
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.validation.constraints.Size

class CreateUser(
  private val userService: UserService = UserService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(CreateUser::class.java)

  private val requiredApiKey: String = System.getenv("APP_API_KEY")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val apiKey = getApiKeyHeader(input)

    if (apiKey == null || apiKey != requiredApiKey) throw JwtTokenException(
      HttpStatusCode.FORBIDDEN.reason,
      ErrorType.JWT_INVALID_EXCEPTION,
      HttpStatusCode.FORBIDDEN
    )

    val createUserRequest: CreateUserRequest =
      Validation.deserializeAndValidate(getBody(input), CreateUserRequest::class)
    val hashedPassword = userService.encryptPassword(createUserRequest.password)

    val createUser = User(
      username = createUserRequest.username,
      hashedPassword = hashedPassword
    )

    userService.persistUser(createUser)
    log.info("User '${createUser.username}' created.")

    userGroupLinkService.addIbaDefaultGroup(createUser.username)
    return generateResponse(HttpStatusCode.CREATED.code)
  }
}

@Serializable
data class CreateUserRequest(
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
