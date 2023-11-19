package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class FilterIngredients : AbstractRequestHandler() {
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService()
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val ingredients = getQueryParameterIngredients(input)

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = userGroupLinkService.getGroups(username)

    val allCocktails = groups.flatMap { group ->
      val groupId = group["groupId"]?.s() ?: throw ValidationException("GroupId is missing") // todo: refactor
      cocktailGroupLinkService.getCocktailsIngredients(groupId)
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
