package com.cocktailfellow.ingredient

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.cocktail.CocktailService
import com.cocktailfellow.cocktail.model.CocktailInfo
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

  private val username = "username"
  private val token = "Bearer token"
  private val method = """
        Muddle mint leaves with sugar and lime juice. Add a splash of soda water and
        fill the glass with cracked ice. Pour the rum over the ice, and fill the glass
        with soda water. Garnish with mint leaves.
    """.trimIndent()

  @BeforeEach
  fun setUp() {
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    cocktailService = Mockito.mock(CocktailService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    context = Mockito.mock(Context::class.java)
    filterIngredients = FilterIngredients(tokenManagement, cocktailService, userGroupLinkService)

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData(username, token)
    )
  }

  private fun createGroup(groupId: String): MutableMap<String, AttributeValue> =
    mutableMapOf("groupId" to AttributeValue.builder().s(groupId).build())

  private fun createIngredients(
    ingredient1: String = "ingredient1",
    ingredient2: String = "ingredient2"
  ): List<Ingredient> =
    listOf(Ingredient(ingredient1, "1cl"), Ingredient(ingredient2, "1cl"))

  private fun createCocktails(ingredients: List<Ingredient>, vararg cocktailNames: String): List<CocktailInfo> =
    cocktailNames.map { name -> CocktailInfo(name, name, method, ingredients) }


  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val username = "username"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2")
    )
    val groups = listOf(
      createGroup("group1"),
      createGroup("group2")
    )
    val ingredients = createIngredients()
    val cocktails1 = createCocktails(ingredients, "cocktail1", "cocktail2")
    val cocktails2 = createCocktails(ingredients, "cocktail3", "cocktail4")

    val expectedCocktailsForBothGroups = cocktails1 + cocktails2

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userGroupLinkService.getGroups(username)).thenReturn(groups)
    `when`(cocktailService.getCocktails("group1")).thenReturn(cocktails1)
    `when`(cocktailService.getCocktails("group2")).thenReturn(cocktails2)

    // When
    val response = filterIngredients.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    val responseObj =
      response.body?.let { JsonConfig.instance.decodeFromString<SearchCocktailsByIngredientsFullResponse>(it) }

    val actualCocktails = responseObj?.result?.cocktails!!
    assertEquals(expectedCocktailsForBothGroups, actualCocktails)

    Mockito.verify(userGroupLinkService, Mockito.atLeastOnce()).getGroups(username)
    Mockito.verify(cocktailService).getCocktails("group1")
    Mockito.verify(cocktailService).getCocktails("group2")
  }

  @Test
  fun `test handleBusinessLogic with groupId parameter`() {
    // Given
    val groupId = "specific_group_id"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "queryStringParameters" to mapOf("ingredients" to "ingredient1,ingredient2", "groupId" to "specific_group_id")
    )
    val ingredients = createIngredients()
    val cocktails = createCocktails(ingredients, "cocktail1", "cocktail2")

    `when`(cocktailService.getCocktails(groupId)).thenReturn(cocktails)

    // When
    val response = filterIngredients.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    val responseObj =
      response.body?.let { JsonConfig.instance.decodeFromString<SearchCocktailsByIngredientsFullResponse>(it) }

    val actualCocktails = responseObj?.result?.cocktails!!
    assertEquals(cocktails, actualCocktails)

    Mockito.verify(cocktailService).getCocktails(groupId)

    Mockito.verify(userGroupLinkService, Mockito.never()).getGroups(any())
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
      {"result":{"cocktails":[]},"loginToken":"Bearer token"}
      """.trimIndent()
    `when`(userGroupLinkService.getGroups("username")).thenReturn(groups)
    `when`(cocktailService.getCocktails(any())).thenReturn(emptyList())

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
