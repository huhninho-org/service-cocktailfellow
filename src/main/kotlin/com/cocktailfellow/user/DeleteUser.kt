package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DeleteUser(
    private val tokenManagement: TokenManagement = TokenManagement(),
    private val userService: UserService = UserService(),
    private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(DeleteUser::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    log.info("Delete user '${tokenManagementData.username}'.")
    userService.deleteUser(tokenManagementData.username)
    userGroupLinkService.deleteAllUserGroupLinks(tokenManagementData.username)
    log.info("User '${tokenManagementData.username}' deleted.")

    return generateResponse(HttpStatusCode.OK.code)
  }
}
