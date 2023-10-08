package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.token.TokenManagement

class GetGroups : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    var headers = input["headers"] as Map<*, *>?
    val authorization = headers?.get("Authorization") as? String

    val loginToken = TokenManagement.validateToken(authorization)
    val username = TokenManagement.getUsername(authorization)

    val groups = UserGroupLinkRepository.getGroups(username)

    val response = GetGroupsResponse(
      groups = groups,
      username = username
    )

    return ApiGatewayResponse.build {
      statusCode = HttpStatusCode.OK.code
      headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      objectBody = objectMapper.writeValueAsString(mapOf("result" to response, "loginToken" to loginToken))
    }
  }
}

data class GetGroupsResponse(
  val groups: List<Map<String, String>>,
  val username: String
)
