package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.user.UserService
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

class CreateGroupLinkTest : BaseTest() {

  private lateinit var createGroupLink: CreateGroupLink
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userService: UserService
  private lateinit var groupService: GroupService
  private lateinit var userGroupLinkService: UserGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    userService = Mockito.mock(UserService::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    createGroupLink = CreateGroupLink(tokenManagement, groupService, userService, userGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val groupId = "group1"
    val username = "user1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to "{\"username\":\"$username\"}"
    )

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("requestingUser", "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(true)
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // When
    val response = createGroupLink.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer invalid-token"),
      "pathParameters" to mapOf("groupId" to "group1"),
      "body" to "{\"username\":\"user1\"}"
    )
    `when`(tokenManagement.validateTokenAndGetData(any())).thenThrow(ValidationException("Invalid token"))

    // When
    val exception = assertThrows<ValidationException> {
      createGroupLink.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid token", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with non-existent user`() {
    // Given
    val groupId = "group1"
    val username = "nonexistentUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to "{\"username\":\"$username\"}"
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("requestingUser", "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(false)

    // When
    val exception = assertThrows<NotFoundException> {
      createGroupLink.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("'USER' not found.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with non-existent group`() {
    // Given
    val groupId = "nonexistentGroup"
    val username = "user1"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to "{\"username\":\"$username\"}"
    )

    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData("requestingUser", "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(true)
    `when`(groupService.doesGroupExist(groupId)).thenReturn(false)

    // When
    val exception = assertThrows<NotFoundException> {
      createGroupLink.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("'GROUP' not found.", exception.message)
  }

  @ParameterizedTest
  @MethodSource("invalidLengthTestData")
  fun `test handleBusinessLogic with invalid field lengths`(value: String) {
    // Given
    val groupId = "group1"
    val username = "user1"
    val createGroupLinkRequest = CreateGroupLinkRequest( value)

    val bodyJson = JsonConfig.instance.encodeToString(CreateGroupLinkRequest.serializer(), createGroupLinkRequest)
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "pathParameters" to mapOf("groupId" to groupId),
      "body" to bodyJson
    )

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("requestingUser", "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(true)
    `when`(groupService.doesGroupExist(groupId)).thenReturn(true)

    // Then
    val exception = assertThrows<ValidationException> {
      createGroupLink.handleBusinessLogic(input, context)
    }

    assertEquals("'username' length should be within 3 to 20 characters.", exception.message)
  }

  companion object {
    @JvmStatic
    fun invalidLengthTestData() = listOf(
      Arguments.of("12"),
      Arguments.of("123456789012345678901")
    )
  }
}
