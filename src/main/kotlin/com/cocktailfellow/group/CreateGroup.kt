package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.group.model.CreateGroupRequest
import com.cocktailfellow.group.model.Group
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.database.UserRepository.Companion.updateGroups
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateGroup : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    var headers = input["headers"] as Map<*, *>?
    val body = input["body"] as String

    val authorization = headers?.get("Authorization") as? String
    val request = JsonConfig.instance.decodeFromString<CreateGroupRequest>(body)
    val groupname = request.groupname

    val loginToken = TokenManagement.validateToken(authorization)
    val username = TokenManagement.getUsername(authorization)

    val groupId = UUID.randomUUID().toString()
    GroupRepository.createGroup(groupId, groupname)

    val groups = UserRepository.getGroupsByUsername(username)
    groups.groups.add(Group(groupId))
    updateGroups(groups, username)

    val response = CreateGroupResponse(
      groupId = groupId,
      username = username,
      groupname = groupname
    )

    return ApiGatewayResponse.build {
      statusCode = HttpStatusCode.CREATED.code
      headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      objectBody = objectMapper.writeValueAsString(mapOf("result" to response, "loginToken" to loginToken))
    }
  }
}

data class CreateGroupResponse(
  val groupId: String,
  val username: String,
  val groupname: String
)
