package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.cocktail.model.Ingredient
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.CocktailGroupLinkRepository
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateCocktail : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val body = input["body"] as String

    val authorization = headers?.get("Authorization") as? String
    val request = JsonConfig.instance.decodeFromString<CreateCocktailRequest>(body)
    val groupId = request.groupId

    val tokenManagementData = TokenManagement.validateTokenAndGetData(authorization)

    if (! GroupRepository.doesGroupExist(groupId)) {
      throw ValidationException("Group does not exist.") // todo: refactor
    }

    val cocktailId = UUID.randomUUID().toString()
    CocktailRepository.createCocktail(cocktailId, request.name, request.method, request.story, request.notes, request.ingredients)
    CocktailGroupLinkRepository.createCocktailToGroupLink(groupId, cocktailId)

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
  val groupId: String,
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
