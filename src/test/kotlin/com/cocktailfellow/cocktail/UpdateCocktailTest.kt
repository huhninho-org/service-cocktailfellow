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

}
