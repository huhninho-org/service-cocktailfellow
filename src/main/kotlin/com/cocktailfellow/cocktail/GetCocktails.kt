package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.CocktailGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class GetCocktails : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!GroupRepository.doesGroupExist(groupId)) {
      throw ValidationException("Group does not exist.") // todo: refactor
    }

    val cocktails = CocktailGroupLinkRepository.getCocktails(groupId)
    val response = GetCocktailsResponse(
      cocktails = cocktails
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetCocktailsResponse(
  val cocktails: List<CocktailInfo>
)
