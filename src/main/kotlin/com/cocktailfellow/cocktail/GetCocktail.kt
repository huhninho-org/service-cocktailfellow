package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable

class GetCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    val cocktail = cocktailService.getCocktail(cocktailId)
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
