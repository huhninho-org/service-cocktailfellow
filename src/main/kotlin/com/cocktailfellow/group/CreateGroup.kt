package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateGroup(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val groupService: GroupService = GroupService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

) : AbstractRequestHandler() {
  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val body = getBody(input)
    val group: CreateGroupRequest

    try {
      group = JsonConfig.instance.decodeFromString(body)
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON body.")
    }
    val groupName = group.groupName

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
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
