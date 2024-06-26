package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.DEFAULT_MAX
import com.cocktailfellow.common.validation.Validation.DEFAULT_MIN
import com.cocktailfellow.common.validation.Validation.MULTILINE_FIELDS
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.validation.constraints.Size

class UpdateCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService(),
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(UpdateCocktail::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val request = Validation.deserializeAndValidate(getBody(input), UpdateCocktailRequest::class)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    log.info("Update cocktail '$cocktailId'.")
    if (cocktailService.isProtected(cocktailId)) {
      log.info("Unable to update protected cocktail '$cocktailId'.")
      throw BadRequestException("Unable to update protected cocktail '$cocktailId'.")
    }

    if (!userGroupLinkService.isMemberOfGroup(tokenManagementData.username, groupId)) {
      log.error("User '${tokenManagementData.username}' is not member of group '$groupId'.")
      throw BadRequestException("User is not member of the given group.")
    }

    if (!cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)) {
      log.error("Cocktail '$cocktailId' is not member of group '$groupId'.")
      throw BadRequestException("Cocktail is not member of the given group.")
    }

    if (request.ingredients.isEmpty()) {
      log.error("Ingredients list cannot be empty for cocktail '$cocktailId'.")
      throw ValidationException("Ingredients list cannot be empty.")
    }

    cocktailService.updateCocktail(
      Cocktail(
        cocktailId,
        request.name,
        request.method,
        request.notes,
        request.ingredients
      )
    )

    val response = UpdateCocktailResponse(
      groupId = groupId,
      cocktailId = cocktailId,
      name = request.name,
      method = request.method,
      notes = request.notes,
      ingredients = request.ingredients
    )
    log.info("Cocktail '$cocktailId' updated.")

    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class UpdateCocktailRequest(
  @field:Size(
    min = DEFAULT_MIN,
    max = DEFAULT_MAX,
    message = "'name' length should be within $DEFAULT_MIN to $DEFAULT_MAX characters."
  )
  val name: String,
  @field:Size(
    max = MULTILINE_FIELDS, message = "'method' exceeds the limit of $MULTILINE_FIELDS characters."
  )
  val method: String? = null,
  @field:Size(
    max = MULTILINE_FIELDS, message = "'notes' exceeds the limit of $MULTILINE_FIELDS characters."
  )
  val notes: String? = null,
  val ingredients: List<Ingredient>
)

@Serializable
data class UpdateCocktailResponse(
  val cocktailId: String,
  val groupId: String,
  val name: String,
  val method: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)
