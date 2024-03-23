package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.model.Group
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class GetGroups(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(GetGroups::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = userGroupLinkService.getGroups(username)

    val responseGroups = if (groups.isNotEmpty()) {
      groups.map { item ->
        val groupId = item["groupId"]?.s() ?: throw ValidationException("GroupId is missing")
        groupService.getGroup(groupId)
      }
    } else {
      emptyList()
    }

    val response = GetGroupsResponse(
      groups = responseGroups
    )
    log.info("Groups fetched for user '$username'.")

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetGroupsResponse(
  val groups: List<Group>?
)
