package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.group.GroupService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class GetCocktailsTest : BaseTest() {

  private lateinit var getCocktails: GetCocktails
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var groupService: GroupService
  private lateinit var cocktailGroupLinkService: CocktailGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    cocktailGroupLinkService = Mockito.mock(CocktailGroupLinkService::class.java)
    getCocktails = GetCocktails(tokenManagement, groupService, cocktailGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val cocktailsList = listOf(CocktailInfo("cocktail1", "Cocktail Name", "Description"))
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)
    `when`(cocktailGroupLinkService.getCocktails(groupId)).thenReturn(cocktailsList)

    // When
    val response = getCocktails.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    // Optionally, assert the contents of the response
  }

  @Test
  fun `test handleBusinessLogic with non-existent group`() {
    // Given
    val groupId = "nonexistentGroup"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(false)

    // When
    val exception = assertThrows<NotFoundException> {
      getCocktails.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("'GROUP' not found.", exception.message)
  }
}
