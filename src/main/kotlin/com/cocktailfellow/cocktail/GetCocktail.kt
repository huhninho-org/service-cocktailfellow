package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class GetCocktail(
  private val cocktailRepository: CocktailRepository = CocktailRepository()
) : AbstractRequestHandler() {
  private val groupService: GroupService = GroupService()

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw ValidationException("Group does not exist.") // todo: refactor
    }
    if (!cocktailRepository.doesCocktailExist(cocktailId)) {
      throw ValidationException("Cocktail does not exist.") // todo: refactor
    }

    val cocktail = cocktailRepository.getCocktail(cocktailId)
    val response = GetCocktailResponse(
      cocktailId = cocktail.cocktailId,
      name = cocktail.name,
      method = cocktail.method,
      story = cocktail.story,
      notes = cocktail.notes,
      ingredients = cocktail.ingredients
    )

    return generateResponse(HttpStatusCode.OK.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class GetCocktailResponse(
  val cocktailId: String,
  val name: String,
  val method: String?,
  val story: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)
