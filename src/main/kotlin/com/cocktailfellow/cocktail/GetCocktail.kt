package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.cocktail.model.Ingredient
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable

class GetCocktail : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val pathParameter = input["pathParameters"] as? Map<*, *>

    val authorization = headers?.get("Authorization") as? String
    val groupId = pathParameter?.get("groupId") as? String ?: throw ValidationException("Invalid group ID.")
    val cocktailId = pathParameter?.get("cocktailId") as? String ?: throw ValidationException("Invalid cocktail ID.")

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (!GroupRepository.doesGroupExist(groupId)) {
      throw ValidationException("Group does not exist.") // todo: refactor
    }
    if (!CocktailRepository.doesCocktailExist(cocktailId)) {
      throw ValidationException("Cocktail does not exist.") // todo: refactor
    }

    val cocktail = CocktailRepository.getCocktail(cocktailId)
    val response = GetCocktailResponse(
      cocktailId = cocktail.cocktailId,
      name = cocktail.name,
      method = cocktail.method,
      story = cocktail.story,
      notes = cocktail.notes,
      ingredients = cocktail.ingredients
    )

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
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
