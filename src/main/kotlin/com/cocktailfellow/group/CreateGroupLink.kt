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
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.CREDENTIALS_MAX
import com.cocktailfellow.common.validation.Validation.USERNAME_MIN
import com.cocktailfellow.user.UserService
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.validation.constraints.Size

class CreateGroupLink(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val groupService: GroupService = GroupService(),
  private val userService: UserService = UserService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(CreateGroupLink::class.java)


  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val usernameToBeLinked = Validation.deserializeAndValidate(getBody(input), CreateGroupLinkRequest::class).username

    val tokenManagementData: TokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!userService.doesUserExist(usernameToBeLinked)) {
      throw NotFoundException(Type.USER)
    }
    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    userGroupLinkService.createUserToGroupLink(usernameToBeLinked, groupId)
    log.info("User '$usernameToBeLinked' linked to group '$groupId'.")

    return generateResponse(HttpStatusCode.CREATED.code, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateGroupLinkRequest(
  @field:Size(
    min = USERNAME_MIN,
    max = CREDENTIALS_MAX,
    message = "'username' length should be within $USERNAME_MIN to $CREDENTIALS_MAX characters."
  )
  val username: String
)
