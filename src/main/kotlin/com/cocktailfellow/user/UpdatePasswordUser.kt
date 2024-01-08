package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationUtil
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.user.model.User
import kotlinx.serialization.Serializable
import javax.validation.constraints.Size

class UpdatePasswordUser(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userService: UserService = UserService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val updatePasswordRequest = ValidationUtil.deserializeAndValidate(getBody(input), UpdatePasswordRequest::class)

    val hashedPassword = userService.encryptPassword(updatePasswordRequest.password)

    userService.updatePasswordUser(User(tokenManagementData.username, hashedPassword))

    return generateResponse(HttpStatusCode.NO_CONTENT.code)
  }
}

@Serializable
  data class UpdatePasswordRequest(
  @field:Size(min = 6, message = "Password must be at least 6 characters.")
  val password: String
)
