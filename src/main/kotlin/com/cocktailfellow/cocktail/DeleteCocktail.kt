package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DeleteCocktail(
  private val tokenManagement: TokenManagement = TokenManagement(),
  private val cocktailGroupLinkService: CocktailGroupLinkService = CocktailGroupLinkService(),
  private val cocktailService: CocktailService = CocktailService(),
  private val userGroupLinkService: UserGroupLinkService = UserGroupLinkService()
) : AbstractRequestHandler() {
  private val log: Logger = LogManager.getLogger(DeleteCocktail::class.java)

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val authorization = getAuthorizationHeader(input)
    val groupId = getPathParameterGroupId(input)
    val cocktailId = getPathParameterCocktailId(input)

    val tokenManagementData: TokenManagementData = tokenManagement.validateTokenAndGetData(authorization)

    log.info("Delete cocktail '$cocktailId'.")
    if (cocktailService.isProtected(cocktailId)) {
      log.info("Unable to delete protected cocktail.")
      throw BadRequestException("Unable to delete protected cocktail '$cocktailId'.")
    }
    if (!userGroupLinkService.isMemberOfGroup(tokenManagementData.username, groupId)) {
      log.error("Delete cocktail failed. User is not member of the given group.")
      throw BadRequestException("User is not member of the given group.")
    }
    if (!cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)) {
      log.error("Delete cocktail failed. Cocktail is not member of the given group.")
      throw BadRequestException("Cocktail is not member of the given group.")
    }

    cocktailService.deleteCocktail(cocktailId)
    cocktailGroupLinkService.deleteLink(cocktailId, groupId)
    log.info("Cocktail '$cocktailId' deleted.")

    return generateResponse(HttpStatusCode.OK.code, tokenManagementData.loginToken)
  }
}
