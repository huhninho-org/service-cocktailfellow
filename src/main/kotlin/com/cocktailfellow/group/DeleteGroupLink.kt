package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.user.UserService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DeleteGroupLink(
    private val tokenManagement: TokenManagement = TokenManagement(),
    private val groupService: GroupService = GroupService(),
    private val userService: UserService = UserService(),
    private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(DeleteGroupLink::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData: TokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    if (!userService.doesUserExist(username)) {
      throw NotFoundException(Type.USER)
    }
    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    userGroupLinkService.deleteUserToGroupLink(username, groupId)
    log.info("Link from user '$username' to group '$groupId' deleted.")

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
