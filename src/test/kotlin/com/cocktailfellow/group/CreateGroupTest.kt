package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class CreateGroupTest : BaseTest() {

  private lateinit var createGroup: CreateGroup
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var groupService: GroupService
  private lateinit var userGroupLinkService: UserGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    createGroup = CreateGroup(tokenManagement, groupService, userGroupLinkService)
  }

  @Test
  fun `test group is created`() {
    // Given
    val input = mapOf(
      "body" to "{\"groupName\":\"testGroup\"}",
      "headers" to mapOf("Authorization" to "Bearer token")
    )

    // When
    Mockito.`when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    val response = createGroup.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
    val responseObj =
      response.body?.let { JsonConfig.instance.decodeFromString<CreateGroupFullResponse>(it) }

    val createdGroup = responseObj?.result!!
    assertEquals("testGroup", createdGroup.groupName)
    assertNotNull(createdGroup.groupId)
  }

  @Test
  fun `test handleBusinessLogic with no group name in request body`() {
    // Given
    val input = mapOf(
      "body" to "{}",
      "headers" to mapOf("Authorization" to "valid-token")
    )

    // When
    val exception = assertThrows<ValidationException> {
      createGroup.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Missing body parameter.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with invalid JSON in request body`() {
    // Given
    val input = mapOf(
      "body" to "invalid json",
      "headers" to mapOf("Authorization" to "valid-token")
    )

    // When
    val exception = assertThrows<ValidationException> {
      createGroup.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid JSON body.", exception.message)
  }

  @Serializable
  data class CreateGroupFullResponse(
    val result: CreateGroupResponse,
    val loginToken: String
  )
}
