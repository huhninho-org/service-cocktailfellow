package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.*
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.validation.Validation
import com.cocktailfellow.common.validation.Validation.DEFAULT_MAX
import com.cocktailfellow.common.validation.Validation.DEFAULT_MIN
import com.cocktailfellow.common.validation.Validation.MULTILINE_FIELDS
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Size

class CreateCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailService: CocktailService = CocktailService(),
  private val groupService: GroupService = GroupService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(CreateCocktail::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)

    val request = Validation.deserializeAndValidate(getBody(input), CreateCocktailRequest::class)

    val tokenManagementData = tokenManagement.validateTokenAndGetData(authorization)
    val cocktailId = UUID.randomUUID().toString()

    log.info("Create cocktail '$cocktailId'.")
    if (groupService.isProtected(groupId)) {
      log.info("Unable to create cocktail due to protected group.")
      throw BadRequestException("Unable to add cocktail to protected group.")
    }

    if (!groupService.doesGroupExist(groupId)) {
      log.error("Group not found '$groupId' for cocktail '$cocktailId'.")
      throw NotFoundException(Type.GROUP)
    }

    if (request.ingredients.isEmpty()) {
      log.error("Ingredients list cannot be empty for cocktail '$cocktailId'.")
      throw ValidationException("Ingredients list cannot be empty.")
    }

    cocktailService.createCocktail(
      groupId, Cocktail(
        cocktailId,
        request.name,
        request.method,
        request.notes,
        request.ingredients
      )
    )

    val response = CreateCocktailResponse(
      groupId = groupId,
      cocktailId = cocktailId,
      name = request.name,
      method = request.method,
      notes = request.notes,
      ingredients = request.ingredients
    )

    log.info("Cocktail '$cocktailId' created.")
    return generateResponse(HttpStatusCode.CREATED.code, response, tokenManagementData.loginToken)
  }
}

@Serializable
data class CreateCocktailRequest(
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
  @field:Valid
  val ingredients: List<Ingredient>
)

@Serializable
data class CreateCocktailResponse(
  val cocktailId: String,
  val groupId: String,
  val name: String,
  val method: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)
