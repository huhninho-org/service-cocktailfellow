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
import com.cocktailfellow.user.database.UserRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class CreateGroupLink : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val pathParameter = input["pathParameters"] as? Map<*, *>
    val body = input["body"] as String

    val authorization = headers?.get("Authorization") as? String
    val request = JsonConfig.instance.decodeFromString<CreateGroupLinkRequest>(body)
    val usernameToBeLinked = request.username
    val groupId = pathParameter?.get("groupId") as? String ?: throw ValidationException("Invalid group ID.")

   TokenManagement.validateTokenOnly(authorization)

    if (!UserRepository.doesUserExist(usernameToBeLinked)) {
      throw ValidationException("The specified user does not exist.") // todo: refactor
    }
    if (!GroupRepository.doesGroupExist(groupId)) {
      throw ValidationException("The specified group does not exist.") // todo: refactor
    }

    UserGroupLinkRepository.createUserToGroupLink(usernameToBeLinked, groupId)

    return generateResponse(HttpStatusCode.CREATED.code)
  }
}

@Serializable
data class CreateGroupLinkRequest(
  val username: String
)
