package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.group.DeleteGroupRequest
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository
import kotlinx.serialization.decodeFromString

class DeleteUser : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?

    val authorization = headers?.get("Authorization") as? String

    TokenManagement.validateToken(authorization)
    val username = TokenManagement.getUsername(authorization)

    UserRepository.deleteUser(username)

    return generateResponse(HttpStatusCode.OK.code)
  }
}
