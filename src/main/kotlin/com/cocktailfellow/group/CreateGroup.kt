package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateGroup : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateGroupRequest>(body)
    val groupName = request.groupName

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groupId = UUID.randomUUID().toString()
    GroupRepository.createGroup(groupId, groupName)

    UserGroupLinkRepository.createUserToGroupLink(username, groupId)

    val response = CreateGroupResponse(
      groupId = groupId,
      username = username,
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
  val username: String,
  val groupName: String
)
