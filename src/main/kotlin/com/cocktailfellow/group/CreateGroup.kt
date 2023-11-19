package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateGroup: AbstractRequestHandler() {
  private val groupService = GroupService()
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateGroupRequest>(body)
    val groupName = request.groupName

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groupId = UUID.randomUUID().toString()
    groupService.createGroup(groupId, groupName)

    userGroupLinkService.createUserToGroupLink(username, groupId)

    val response = CreateGroupResponse(
      groupId = groupId,
      groupName = groupName
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateGroupRequest(
  val groupName: String
)

@Serializable
data class CreateGroupResponse(
  val groupId: String,
  val groupName: String
)
