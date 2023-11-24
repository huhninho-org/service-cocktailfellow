package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class GetCocktailTest : BaseTest() {
  private lateinit var getCocktail: GetCocktail
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var cocktailRepository: CocktailRepository
  private lateinit var groupService: GroupService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    cocktailRepository = Mockito.mock(CocktailRepository::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    getCocktail = GetCocktail(tokenManagement, cocktailRepository, groupService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val cocktail = Cocktail(cocktailId, "Cocktail Name", "Method", "Story", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)
    `when`(cocktailRepository.doesCocktailExist(cocktailId)).thenReturn(true)
    `when`(cocktailRepository.getCocktail(cocktailId)).thenReturn(cocktail)

    // When
    val response = getCocktail.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with non-existent group`() {
    // Given
    val groupId = "nonexistentGroup"
    val cocktailId = "cocktail1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(false)

    // When
    val exception = assertThrows<ValidationException> {
      getCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Group does not exist.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with non-existent cocktail`() {
    // Given
    val groupId = "group1"
    val cocktailId = "nonexistentCocktail"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)
    `when`(cocktailRepository.doesCocktailExist(cocktailId)).thenReturn(false)

    // When
    val exception = assertThrows<ValidationException> {
      getCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Cocktail does not exist.", exception.message)
  }
}
