package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository

class DeleteGroupLink : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val pathParameter = input["pathParameters"] as? Map<*, *>

    val authorization = headers?.get("Authorization") as? String
    val groupId = pathParameter?.get("groupId") as? String ?: throw ValidationException("Invalid group ID.")

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    if (!UserRepository.doesUserExist(username)) {
      throw ValidationException("The specified user does not exist.") // todo: refactor
    }
    if (!GroupRepository.doesGroupExist(groupId)) {
      throw ValidationException("The specified group does not exist.") // todo: refactor
    }

    UserGroupLinkRepository.deleteUserToGroupLink(username, groupId)

    return generateResponse(HttpStatusCode.NO_CONTENT.code)
  }
}
