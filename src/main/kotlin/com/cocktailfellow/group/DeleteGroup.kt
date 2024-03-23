package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DeleteGroup(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(DeleteGroup::class.java)


  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData: TokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!userGroupLinkService.isMemberOfGroup(tokenManagementData.username, groupId)) {
      throw BadRequestException("User is not member of the given group.")
    }

    log.info("Delete group '$groupId'.")
    groupService.deleteGroup(groupId)
    userGroupLinkService.deleteAllLinksForGroup(groupId)
    log.info("Group '$groupId' deleted.")

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
