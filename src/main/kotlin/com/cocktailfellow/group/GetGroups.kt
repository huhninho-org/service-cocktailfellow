package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class GetGroups : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = UserGroupLinkRepository.getGroups(username)

    val response = GetGroupsResponse(
      groups = groups
    )

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetGroupsResponse(
  val groups: List<Map<String, String>>
)
