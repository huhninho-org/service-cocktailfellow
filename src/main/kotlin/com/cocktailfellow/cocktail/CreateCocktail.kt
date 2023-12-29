package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.*
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateCocktailRequest>(body)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw NotFoundException(Type.GROUP)
    }

    if (request.ingredients.isEmpty()) {
      throw ValidationException("Ingredients list cannot be empty.")
    }

    val cocktailId = UUID.randomUUID().toString()
    cocktailService.createCocktail(
      groupId, Cocktail(
        cocktailId,
        request.name,
        request.method,
        request.story,
        request.notes,
        request.ingredients
      )
    )

    val response = CreateCocktailResponse(
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
data class CreateCocktailRequest(
  val name: String,
  val method: String? = null,
  val story: String? = null,
  val notes: String? = null,
  val ingredients: List<Ingredient>
)

@Serializable
data class CreateCocktailResponse(
  val cocktailId: String,
  val groupId: String,
  val name: String,
  val method: String?,
  val story: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)
