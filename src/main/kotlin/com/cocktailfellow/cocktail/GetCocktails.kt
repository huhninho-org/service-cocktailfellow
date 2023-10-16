package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.CocktailGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class GetCocktails : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val pathParameter = input["pathParameters"] as? Map<*, *>

    val authorization = headers?.get("Authorization") as? String
    val groupId = pathParameter?.get("groupId") as? String ?: throw ValidationException("Invalid group ID.")

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
  val cocktails: List<Cocktail>
)
