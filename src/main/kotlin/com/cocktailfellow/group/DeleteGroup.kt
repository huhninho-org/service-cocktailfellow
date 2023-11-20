package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagementDeprecated
import com.cocktailfellow.common.token.TokenManagementData

class DeleteGroup : AbstractRequestHandler() {
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
  private val groupService: GroupService = GroupService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData: TokenManagementData = TokenManagementDeprecated.validateTokenAndGetData(authorization)

    if (!userGroupLinkService.isMemberOfGroup(tokenManagementData.username, groupId)) {
      throw ValidationException("User is not allowed to delete the group.") // todo: refactor
    }
    groupService.deleteGroup(groupId)
    userGroupLinkService.deleteAllLinksForGroup(groupId)

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
