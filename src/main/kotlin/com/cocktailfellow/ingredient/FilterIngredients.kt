package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.CocktailService
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import kotlinx.serialization.Serializable

class FilterIngredients (
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val ingredients = getQueryParameterIngredients(input)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    val groups = userGroupLinkService.getGroups(username)
    if (groups.isNullOrEmpty())
      throw BadRequestException("User has no linked groups.")

    val allCocktails = groups.flatMap { group ->
      val groupId = group["groupId"]?.s() ?: throw NotFoundException(Type.GROUP)
      cocktailService.getCocktailsIngredients(groupId)
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
