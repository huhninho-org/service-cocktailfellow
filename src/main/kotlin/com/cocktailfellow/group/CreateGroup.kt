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
    val headers = input["headers"] as Map<*, *>?
    val body = input["body"] as String

    val authorization = headers?.get("Authorization") as? String
    val request = JsonConfig.instance.decodeFromString<CreateGroupRequest>(body)
    val groupName = request.groupName

    val loginToken = TokenManagement.validateToken(authorization)
    val username = TokenManagement.getUsername(authorization)

    val groupId = UUID.randomUUID().toString()
    GroupRepository.createGroup(groupId, groupName)

    UserGroupLinkRepository.linkUserToGroup(username, groupId)

    val response = CreateGroupResponse(
      groupId = groupId,
      username = username,
      groupName = groupName
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, loginToken)
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