package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class DeleteCocktailTest : BaseTest() {

  private lateinit var deleteCocktail: DeleteCocktail
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var cocktailRepository: CocktailRepository
  private lateinit var cocktailGroupLinkService: CocktailGroupLinkService
  private lateinit var userGroupLinkService: UserGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    cocktailRepository = Mockito.mock(CocktailRepository::class.java)
    cocktailGroupLinkService = Mockito.mock(CocktailGroupLinkService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    deleteCocktail = DeleteCocktail(tokenManagement, cocktailGroupLinkService, cocktailRepository, userGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val username = "user1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userGroupLinkService.isMemberOfGroup(username, groupId)).thenReturn(true)
    `when`(cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)).thenReturn(true)

    // When
    val response = deleteCocktail.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic when user is not a member of the group`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val username = "user1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userGroupLinkService.isMemberOfGroup(username, groupId)).thenReturn(false)

    // When
    val exception = assertThrows<BadRequestException> {
      deleteCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("User is not member of the given group.", exception.message)
  }
}
