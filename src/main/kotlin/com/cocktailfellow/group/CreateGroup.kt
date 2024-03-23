package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.DEFAULT_MAX
import com.cocktailfellow.common.validation.Validation.DEFAULT_MIN
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import javax.validation.constraints.Size

class CreateGroup(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val groupService: GroupService = GroupService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(CreateGroup::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)

    val group = Validation.deserializeAndValidate(getBody(input), CreateGroupRequest::class)
    val groupName = group.groupName

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groupId = UUID.randomUUID().toString()

    log.info("Create group '${group.groupName}' for user '${tokenManagementData.username}'.")
    groupService.createGroup(groupId, groupName)
    userGroupLinkService.createUserToGroupLink(username, groupId)
    log.info("Group '${tokenManagementData.username}' created.")

    val response = CreateGroupResponse(
      groupId = groupId,
      groupName = groupName
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateGroupRequest(
  @field:Size(
    min = DEFAULT_MIN, max = DEFAULT_MAX, message = "'groupName' length should be within $DEFAULT_MIN to $DEFAULT_MAX characters."
  )
  val groupName: String
)

@Serializable
data class CreateGroupResponse(
  val groupId: String,
  val groupName: String
)
