package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagementDeprecated
import com.cocktailfellow.user.common.UserService

class DeleteUser(
  private val userService: UserService = UserService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val tokenManagementData = TokenManagementDeprecated.validateTokenAndGetData(authorization)

    userService.deleteUser(tokenManagementData.username)
    userGroupLinkService.deleteAllUserGroupLinks(tokenManagementData.username)

    return generateResponse(HttpStatusCode.OK.code)
  }
}
