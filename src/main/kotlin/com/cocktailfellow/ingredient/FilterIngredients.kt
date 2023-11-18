package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.CocktailGroupLinkRepository
import com.cocktailfellow.common.database.UserGroupLinkRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class FilterIngredients : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val ingredients = getQueryParameterIngredients(input)

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = UserGroupLinkRepository.getGroups(username)

    val allCocktails = groups.flatMap { group ->
      val groupId = group["groupId"] ?: throw ValidationException("GroupId is missing") // todo: refactor
      CocktailGroupLinkRepository.getCocktailsIngredients(groupId)
    }

    val filteredCocktails = allCocktails.filter { cocktail ->
      ingredients.all { ingredient ->
        cocktail.ingredients.any { it.ingredientName.contains(ingredient, ignoreCase = true) }
      }
    }

    val response = SearchCocktailsByIngredientsResponse(
      cocktails = filteredCocktails
    )

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class SearchCocktailsByIngredientsResponse(
  val cocktails: List<CocktailIngredients>
)
