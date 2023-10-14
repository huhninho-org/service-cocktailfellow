package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository

class DeleteUser : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?

    val authorization = headers?.get("Authorization") as? String
    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    UserRepository.deleteUser(tokenManagementData.username)

    return generateResponse(HttpStatusCode.OK.code)
  }
}
