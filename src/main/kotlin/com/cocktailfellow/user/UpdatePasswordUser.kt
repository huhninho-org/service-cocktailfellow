package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.CREDENTIALS_MAX
import com.cocktailfellow.common.validation.Validation.PASSWORD_MIN
import com.cocktailfellow.user.model.User
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.validation.constraints.Size

class UpdatePasswordUser(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userService: UserService = UserService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(UpdatePasswordUser::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val updatePasswordRequest = Validation.deserializeAndValidate(getBody(input), UpdatePasswordRequest::class)

    log.info("Update password for user '${tokenManagementData.username}'.")
    val hashedPassword = userService.encryptPassword(updatePasswordRequest.password)
    userService.updatePasswordUser(User(tokenManagementData.username, hashedPassword))
    log.info("Password for user '${tokenManagementData.username}' updated.")

    return generateResponse(HttpStatusCode.NO_CONTENT.code)
  }
}

@Serializable
  data class UpdatePasswordRequest(
  @field:Size(
    min = PASSWORD_MIN,
    max = CREDENTIALS_MAX,
    message = "'password' length should be within $PASSWORD_MIN to $CREDENTIALS_MAX characters."
  )
  val password: String
)
