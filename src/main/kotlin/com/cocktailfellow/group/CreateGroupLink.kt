package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.user.UserService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class CreateGroupLink(
    private val tokenManagement: TokenManagement = TokenManagement(),
    private val groupService: GroupService = GroupService(),
    private val userService: UserService = UserService(),
    private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateGroupLinkRequest>(body)
    val usernameToBeLinked = request.username

    val tokenManagementData: TokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!userService.doesUserExist(usernameToBeLinked)) {
      throw NotFoundException(Type.USER)
    }
    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    userGroupLinkService.createUserToGroupLink(usernameToBeLinked, groupId)

    return generateResponse(HttpStatusCode.CREATED.code, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateGroupLinkRequest(
  val username: String
)
