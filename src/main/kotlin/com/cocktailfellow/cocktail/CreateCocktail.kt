package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.*

class CreateCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailRepository: CocktailRepository = CocktailRepository(),
  private val groupService: GroupService = GroupService(),
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService()
) : AbstractRequestHandler() {

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val body = getBody(input)

    val request = JsonConfig.instance.decodeFromString<CreateCocktailRequest>(body)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    if (!groupService.doesGroupExist(groupId)) {
      throw ValidationException("Group does not exist.") // todo: refactor
    }

    if (request.ingredients.isEmpty()) {
      throw ValidationException("Ingredients list cannot be empty.")
    }

    val cocktailId = UUID.randomUUID().toString()
    cocktailRepository.createCocktail(
      cocktailId,
      request.name,
      request.method,
      request.story,
      request.notes,
      request.ingredients
    )
    cocktailGroupLinkService.createCocktailToGroupLink(groupId, cocktailId)

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
