package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.group.GroupService
import com.cocktailfellow.ingredient.model.Ingredient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class CreateCocktailTest : BaseTest() {

  private lateinit var createCocktail: CreateCocktail
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var cocktailRepository: CocktailRepository
  private lateinit var cocktailService: CocktailService
  private lateinit var groupService: GroupService
  private lateinit var cocktailGroupLinkService: CocktailGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    cocktailRepository = Mockito.mock(CocktailRepository::class.java)
    cocktailService = Mockito.mock(CocktailService::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    cocktailGroupLinkService = Mockito.mock(CocktailGroupLinkService::class.java)
    createCocktail = CreateCocktail(tokenManagement, cocktailService, groupService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val cocktailRequest = CreateCocktailRequest("Cocktail", "Method", "Story", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to JsonConfig.instance.encodeToString(CreateCocktailRequest.serializer(), cocktailRequest)
    )

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // When
    val response = createCocktail.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with non-existent group`() {
    // Given
    val groupId = "nonexistentGroup"
    val cocktailRequest = CreateCocktailRequest("Cocktail", "Method", "Story", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to JsonConfig.instance.encodeToString(CreateCocktailRequest.serializer(), cocktailRequest)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(false)

    // When
    val exception = assertThrows<NotFoundException> {
      createCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("'GROUP' not found.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with no body parameter`() {
    // Given
    val groupId = "group1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId)
      // Note: No body is provided here
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // When
    val exception = assertThrows<ValidationException> {
      createCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Missing body parameter.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with body missing ingredients`() {
    // Given
    val groupId = "group1"
    val cocktailRequestWithoutIngredients = CreateCocktailRequest(
      name = "Cocktail",
      method = "Method",
      story = "Story",
      notes = "Notes",
      ingredients = emptyList() // Missing ingredients
    )
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to JsonConfig.instance.encodeToString(CreateCocktailRequest.serializer(), cocktailRequestWithoutIngredients)
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // When
    val exception = assertThrows<ValidationException> {
      createCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Ingredients list cannot be empty.", exception.message)
  }

}
