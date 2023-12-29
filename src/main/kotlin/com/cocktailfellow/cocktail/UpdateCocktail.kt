package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.*
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class UpdateCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<UpdateCocktailRequest>(body)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    if (request.ingredients.isEmpty()) {
      throw ValidationException("Ingredients list cannot be empty.")
    }

    // todo: implement updateCocktail
    /*
    cocktailService.updateCocktail(
      cocktailId,
      request.name,
      request.method,
      request.story,
      request.notes,
      request.ingredients
    )
     */

    val response = UpdateCocktailResponse(
      groupId = groupId,
      cocktailId = cocktailId,
      name = request.name,
      method = request.method,
      story = request.story,
      notes = request.notes,
      ingredients = request.ingredients
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class UpdateCocktailRequest(
  val name: String,
  val method: String? = null,
  val story: String? = null,
  val notes: String? = null,
  val ingredients: List<Ingredient>
)

@Serializable
data class UpdateCocktailResponse(
  val cocktailId: String,
  val groupId: String,
  val name: String,
  val method: String?,
  val story: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)
