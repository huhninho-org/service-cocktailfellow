package com.cocktailfellow.cocktail

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.CocktailGroupLinkService
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.ingredient.model.Ingredient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.util.*

class UpdateCocktailTest {

  private lateinit var updateCocktail: UpdateCocktail
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var cocktailService: CocktailService
  private lateinit var userGroupLinkService: UserGroupLinkService
  private lateinit var cocktailGroupLinkService: CocktailGroupLinkService

  @BeforeEach
  fun setup() {
    context = mock()
    tokenManagement = mock()
    cocktailService = mock()
    userGroupLinkService = mock()
    cocktailGroupLinkService = mock()
    updateCocktail = UpdateCocktail(tokenManagement, cocktailService, userGroupLinkService, cocktailGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val updateRequest = UpdateCocktailRequest(
      name = "Updated Cocktail",
      method = "Updated Method",
      story = "Updated Story",
      notes = "Updated Notes",
      ingredients = listOf(Ingredient("Ingredient 1", "2cl"))
    )
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId),
      "body" to JsonConfig.instance.encodeToString(UpdateCocktailRequest.serializer(), updateRequest)
    )

    Mockito.`when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup("username", groupId)).thenReturn(true)
    Mockito.`when`(cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)).thenReturn(true)

    // When
    val response = updateCocktail.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic when user is not a member of the group`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val updateRequest = UpdateCocktailRequest(
      name = "Updated Cocktail",
      method = "Updated Method",
      story = "Updated Story",
      notes = "Updated Notes",
      ingredients = listOf(Ingredient("Ingredient 1", "2cl"))
    )
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId),
      "body" to JsonConfig.instance.encodeToString(UpdateCocktailRequest.serializer(), updateRequest)
    )

    Mockito.`when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup("username", groupId)).thenReturn(false)

    // When
    val exception = assertThrows<BadRequestException> {
      updateCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("User is not member of the given group.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic when cocktail is not linked to the group`() {
    // Given
    val groupId = UUID.randomUUID().toString()
    val cocktailId = UUID.randomUUID().toString()
    val updateCocktailRequest = UpdateCocktailRequest("Cocktail", "Method", "Story", "Notes", listOf(Ingredient("Ingredient 1", "1cl")))
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId),
      "body" to JsonConfig.instance.encodeToString(UpdateCocktailRequest.serializer(), updateCocktailRequest)
    )

    Mockito.`when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup("username", groupId)).thenReturn(true)
    Mockito.`when`(cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)).thenReturn(false)

    // When
    val exception = assertThrows<BadRequestException> {
      updateCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Cocktail is not member of the given group.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with empty ingredients list`() {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val updateRequest = UpdateCocktailRequest(
      name = "Updated Cocktail",
      method = "Updated Method",
      story = "Updated Story",
      notes = "Updated Notes",
      ingredients = emptyList()
    )
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId),
      "body" to JsonConfig.instance.encodeToString(UpdateCocktailRequest.serializer(), updateRequest)
    )

    Mockito.`when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup("username", groupId)).thenReturn(true)
    Mockito.`when`(cocktailGroupLinkService.isMemberOfGroup(cocktailId, groupId)).thenReturn(true)

    // When
    val exception = assertThrows<ValidationException> {
      updateCocktail.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Ingredients list cannot be empty.", exception.message)
  }

  @ParameterizedTest
  @MethodSource("invalidLengthTestData")
  fun `test handleBusinessLogic with invalid field lengths`(field: String, value: String, expectedMessage: String) {
    // Given
    val groupId = "group1"
    val cocktailId = "cocktail1"
    val ingredients = listOf(
      Ingredient("White Rum", "50ml"),
      Ingredient("Lime Juice", "20ml")
    )

    val cocktailRequest = when (field) {
      "name" -> CreateCocktailRequest(value , "some method", "Some Story.", "Some Notes.", ingredients)
      "method" -> CreateCocktailRequest("some name", value, "Some Story.", "Some Notes.", ingredients)
      "story" -> CreateCocktailRequest("some name", "Some Method.", value, "Some Notes.", ingredients)
      "notes" -> CreateCocktailRequest("some name", "Some Method.", "Some Story.", value, ingredients)
      else -> throw IllegalArgumentException("Invalid field for test")
    }
    val bodyJson = JsonConfig.instance.encodeToString(CreateCocktailRequest.serializer(), cocktailRequest)

    val input = mapOf(
      "headers" to mapOf("x-api-key" to "your-api-key"),
      "pathParameters" to mapOf("groupId" to groupId, "cocktailId" to cocktailId),
      "body" to bodyJson
    )

    // When
    Mockito.`when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup("username", groupId)).thenReturn(false)

    // Then
    val exception = assertThrows<ValidationException> {
      updateCocktail.handleBusinessLogic(input, context)
    }
    assertEquals(expectedMessage, exception.message)
  }

  companion object {
    @JvmStatic
    fun invalidLengthTestData() = listOf(
      Arguments.of("name", "a".repeat(51), "'name' length should be within 3 to 50 characters."),
      Arguments.of("name", "12", "'name' length should be within 3 to 50 characters."),
      Arguments.of("method", "a".repeat(256), "'method' exceeds the limit of 255 characters."),
      Arguments.of("story", "a".repeat(256), "'story' exceeds the limit of 255 characters."),
      Arguments.of("notes", "a".repeat(256), "'notes' exceeds the limit of 255 characters.")
    )
  }
}
