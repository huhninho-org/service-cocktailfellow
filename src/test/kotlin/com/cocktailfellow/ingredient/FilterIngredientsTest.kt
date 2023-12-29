package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.CocktailService
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class FilterIngredientsTest : BaseTest() {

  private lateinit var filterIngredients: FilterIngredients
  private lateinit var cocktailService: CocktailService
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userGroupLinkService: UserGroupLinkService
  private lateinit var context: Context

  @BeforeEach
  fun setUp() {
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    cocktailService = Mockito.mock(CocktailService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    context = Mockito.mock(Context::class.java)
    filterIngredients = FilterIngredients(tokenManagement, cocktailService, userGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2")
    )
    val groups = listOf(
      mutableMapOf("groupId" to AttributeValue.builder().s("group1").build()),
      mutableMapOf("groupId" to AttributeValue.builder().s("group2").build())
    )
    val ingredients = listOf(
      Ingredient("ingredient1", "1cl"),
      Ingredient("ingredient2", "1cl")
    )
    val cocktails1 = listOf(
      CocktailIngredients("cocktail1", "Cocktail 1", ingredients),
      CocktailIngredients("cocktail2", "Cocktail 2", ingredients)
    )
    val cocktails2 = listOf(
      CocktailIngredients("cocktail3", "Cocktail 3", ingredients),
      CocktailIngredients("cocktail4", "Cocktail 4", ingredients)
    )

    val expectedCocktailsForBothGroups = cocktails1 + cocktails2

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups("username")).thenReturn(groups)
    `when`(cocktailService.getCocktailsIngredients("group1")).thenReturn(cocktails1)
    `when`(cocktailService.getCocktailsIngredients("group2")).thenReturn(cocktails2)

    // When
    val response = filterIngredients.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    val responseObj =
      response.body?.let { JsonConfig.instance.decodeFromString<SearchCocktailsByIngredientsFullResponse>(it) }

    val actualCocktails = responseObj?.result?.cocktails!!
    assertEquals(expectedCocktailsForBothGroups, actualCocktails)
  }

  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer invalid_token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2")
    )
    `when`(tokenManagement.validateTokenAndGetData(any())).thenThrow(ValidationException("Invalid token"))

    // When
    val exception = assertThrows<ValidationException> {
      filterIngredients.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid token", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with no matching parameters`() {
    // Given
    val input = emptyMap<String, Any>()

    // When
    val exception = assertThrows<ValidationException> {
      filterIngredients.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Missing query parameters.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with no groups for user`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer valid_token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2")
    )
    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups(any())).thenReturn(emptyList())

    // When
    val exception = assertThrows<BadRequestException> {
      filterIngredients.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("User has no linked groups.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with no ingredients`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer valid_token"),
      "queryStringParameters" to mapOf("ingredients" to "")
    )
    val groups = listOf(
      mutableMapOf("groupId" to AttributeValue.builder().s("group1").build()),
      mutableMapOf("groupId" to AttributeValue.builder().s("group2").build())
    )
    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups("username")).thenReturn(groups)

    // When
    val exception = assertThrows<ValidationException> {
      filterIngredients.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Missing ingredients parameter.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with no cocktails for given ingredients`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer valid_token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2")
    )
    val groups = listOf(
      mutableMapOf("groupId" to AttributeValue.builder().s("group1").build()),
      mutableMapOf("groupId" to AttributeValue.builder().s("group2").build())
    )
    val expectedResponse = """
      {"result":{"cocktails":[]},"loginToken":"token"}
      """.trimIndent()
    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups("username")).thenReturn(groups)
    `when`(cocktailService.getCocktailsIngredients(any())).thenReturn(emptyList())

    // When
    val response = filterIngredients.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)

    assertEquals(expectedResponse, response.body)
  }

  @Serializable
  data class SearchCocktailsByIngredientsFullResponse(
    val result: SearchCocktailsByIngredientsResponse,
    val loginToken: String
  )
}
