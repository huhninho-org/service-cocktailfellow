package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.token.TokenManagementData
import com.cocktailfellow.user.common.UserService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class CreateGroupLink : AbstractRequestHandler() {
  private val groupService: GroupService = GroupService()
  private val userService: UserService = UserService()
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()


  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateGroupLinkRequest>(body)
    val usernameToBeLinked = request.username

    val tokenManagementData: TokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!userService.doesUserExist(usernameToBeLinked)) {
      throw ValidationException("The specified user does not exist.") // todo: refactor
    }
    if (!groupService.doesGroupExist(groupId)) {
      throw ValidationException("The specified group does not exist.") // todo: refactor
    }

    userGroupLinkService.createUserToGroupLink(usernameToBeLinked, groupId)

    return generateResponse(HttpStatusCode.CREATED.code, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateGroupLinkRequest(
  val username: String
)
