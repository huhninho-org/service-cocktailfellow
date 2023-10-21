package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.CocktailGroupLinkRepository
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.token.TokenManagementData

class DeleteCocktail : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val tokenManagementData: TokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!UserGroupLinkRepository.isMemberOfGroup(tokenManagementData.username, groupId)) {
      throw ValidationException("User is not member the group.") // todo: refactor
    }
    if (!CocktailGroupLinkRepository.isMemberOfGroup(cocktailId, groupId)) {
      throw ValidationException("Cocktail is not member the group.") // todo: refactor
    }

    CocktailRepository.deleteCocktail(cocktailId)
    CocktailGroupLinkRepository.deleteLink(cocktailId, groupId)

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
