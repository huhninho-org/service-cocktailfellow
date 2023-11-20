package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagementDeprecated
import kotlinx.serialization.Serializable

class GetGroups : AbstractRequestHandler() {
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
  private val groupService: GroupService = GroupService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)

    val tokenManagementData = TokenManagementDeprecated.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = userGroupLinkService.getGroups(username)
    if (groups.isNullOrEmpty())
      throw ValidationException("User has no groups.") // todo: refactor

    val groupNames = groups.map { item ->
        val groupId = item["groupId"]?.s() ?: throw ValidationException("GroupId is missing")
        val groupName = groupService.getGroupName(groupId)
        mapOf("groupId" to groupId, "groupName" to groupName)
      }


    val response = GetGroupsResponse(
      groups = groupNames
    )

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetGroupsResponse(
  val groups: List<Map<String, String>>
)
