package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.CocktailService
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class FilterIngredients(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(FilterIngredients::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val filterIngredients = getQueryParameterIngredients(input)
    val filterGroupId = getOptionalQueryParameterGroupId(input)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val username = tokenManagementData.username

    log.info("Filter cocktails for user '$username'.")
    val unfilteredCocktails = if (filterGroupId.isNullOrEmpty()) {
      val groups = userGroupLinkService.getGroups(username)
      if (groups.isEmpty())
        throw BadRequestException("User has no linked groups.")

      groups.flatMap { group ->
        val groupId = group["groupId"]?.s() ?: throw NotFoundException(Type.GROUP)
        cocktailService.getCocktails(groupId)
      }
    } else {
      cocktailService.getCocktails(filterGroupId)
    }

    val filteredCocktails = unfilteredCocktails.filter { cocktail ->
      filterIngredients.all { ingredient ->
        cocktail.ingredients.any { it.ingredientName.contains(ingredient, ignoreCase = true) }
      }
    }

    val response = SearchCocktailsByIngredientsResponse(
      cocktails = filteredCocktails
    )
    log.info("Cocktails filtered for user '$username'.")

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class SearchCocktailsByIngredientsResponse(
  val cocktails: List<CocktailInfo>
)
