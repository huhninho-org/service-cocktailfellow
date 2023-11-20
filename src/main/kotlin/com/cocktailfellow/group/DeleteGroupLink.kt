package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagementDeprecated
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.user.common.UserService

class DeleteGroupLink : AbstractRequestHandler() {
  private val groupService: GroupService = GroupService()
  private val userService: UserService = UserService()
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData: TokenManagementData = TokenManagementDeprecated.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    if (!userService.doesUserExist(username)) {
      throw ValidationException("The specified user does not exist.") // todo: refactor
    }
    if (!groupService.doesGroupExist(groupId)) {
      throw ValidationException("The specified group does not exist.") // todo: refactor
    }

    userGroupLinkService.deleteUserToGroupLink(username, groupId)

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
