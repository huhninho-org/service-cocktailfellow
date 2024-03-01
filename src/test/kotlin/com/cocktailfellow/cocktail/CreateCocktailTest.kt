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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
    val cocktailRequest =
      CreateCocktailRequest("Cocktail", "Method", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
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
    val cocktailRequest =
      CreateCocktailRequest("Cocktail", "Method", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
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
      notes = "Notes",
      ingredients = emptyList() // Missing ingredients
    )
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to JsonConfig.instance.encodeToString(
        CreateCocktailRequest.serializer(),
        cocktailRequestWithoutIngredients
      )
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

  @ParameterizedTest
  @MethodSource("invalidLengthTestData")
  fun `test handleBusinessLogic with invalid field lengths`(field: String, value: String, expectedMessage: String) {
    // Given
    val groupId = "group1"
    val ingredients = listOf(
      Ingredient("White Rum", "50ml"),
      Ingredient("Lime Juice", "20ml")
    )

    val cocktailRequest = when (field) {
      "name" -> CreateCocktailRequest(value, "some name", "Some Notes.", ingredients)
      "method" -> CreateCocktailRequest("some name", value, "Some Notes.", ingredients)
      "notes" -> CreateCocktailRequest("some name", "Some Method.", value, ingredients)
      "ingredient" -> CreateCocktailRequest(
        "some name", "Some Method.", "Some Notes.", listOf(
          Ingredient(value, "50ml")
        )
      )

      "amount" -> CreateCocktailRequest(
        "some name", "Some Method.", "Some Notes.", listOf(
          Ingredient("White Rum", value)
        )
      )

      else -> throw IllegalArgumentException("Invalid field for test")
    }
    val bodyJson = JsonConfig.instance.encodeToString(CreateCocktailRequest.serializer(), cocktailRequest)

    val input = mapOf(
      "headers" to mapOf("x-api-key" to "your-api-key"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to bodyJson
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // Then
    val exception = assertThrows<ValidationException> {
      createCocktail.handleBusinessLogic(input, context)
    }
    assertEquals(expectedMessage, exception.message)
  }

  companion object {
    @JvmStatic
    fun invalidLengthTestData() = listOf(
      Arguments.of("name", "a".repeat(51), "'name' length should be within 3 to 50 characters."),
      Arguments.of("name", "12", "'name' length should be within 3 to 50 characters."),
      Arguments.of("method", "a".repeat(256), "'method' exceeds the limit of 255 characters."),
      Arguments.of("notes", "a".repeat(256), "'notes' exceeds the limit of 255 characters."),
      Arguments.of("ingredient", "", "must not be empty"),
      Arguments.of("ingredient", "a".repeat(51), "'ingredientName' exceeds the limit of 50 characters."),
      Arguments.of("amount", "", "must not be empty"),
      Arguments.of("amount", "a".repeat(51), "'amount' exceeds the limit of 50 characters.")
    )
  }
}
