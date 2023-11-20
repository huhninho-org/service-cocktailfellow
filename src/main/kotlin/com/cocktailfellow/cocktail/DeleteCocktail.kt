package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagementDeprecated
import com.cocktailfellow.common.token.TokenManagementData

class DeleteCocktail(
  private val cocktailRepository: CocktailRepository = CocktailRepository()
) : AbstractRequestHandler() {
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService()

  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val tokenManagementData: TokenManagementData = TokenManagementDeprecated.validateTokenAndGetData(authorization)

    if (!userGroupLinkService.isMemberOfGroup(tokenManagementData.username, groupId)) {
      throw ValidationException("User is not member the group.") // todo: refactor
    }
    if (!cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)) {
      throw ValidationException("Cocktail is not member the group.") // todo: refactor
    }

    cocktailRepository.deleteCocktail(cocktailId)
    cocktailGroupLinkService.deleteLink(cocktailId, groupId)

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
