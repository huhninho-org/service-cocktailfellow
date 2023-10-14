package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.token.TokenManagementData
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class DeleteGroup : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val body = input["body"] as String

    val authorization = headers?.get("Authorization") as? String
    val request = JsonConfig.instance.decodeFromString<DeleteGroupRequest>(body)
    val groupId = request.groupId

    val tokenManagementData: TokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!UserGroupLinkRepository.isMemberOfGroup(tokenManagementData.username, groupId)) {
      throw ValidationException("User is not allowed to delete the group.") // todo: refactor
    }
    GroupRepository.deleteGroup(groupId)

    return generateResponse(HttpStatusCode.OK.code)
  }
}

@Serializable
data class DeleteGroupRequest(
  val groupId: String
)
