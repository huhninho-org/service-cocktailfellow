package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.GroupService
import kotlinx.serialization.Serializable

class GetCocktails(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val groupService: GroupService = GroupService(),
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    val cocktails = cocktailGroupLinkService.getCocktails(groupId)
    val response = GetCocktailsResponse(
      cocktails = cocktails
    )

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetCocktailsResponse(
  val cocktails: List<CocktailInfo>
)
