package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.user.common.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class DeleteUserTestUser : BaseTest() {

  private lateinit var deleteUser: DeleteUser
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userService: UserService
  private lateinit var userGroupLinkService: UserGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    userService = Mockito.mock(UserService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    deleteUser = DeleteUser(tokenManagement, userService, userGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid request`() {
    // Given
    val username = "testUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token")
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(false)
    val response = deleteUser.handleBusinessLogic(input, context)

    // Then
    verify(userService).deleteUser(any())
    verify(userGroupLinkService).deleteAllUserGroupLinks(any())
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with non-existing user`() {
    // Given
    val username = "nonExistingUser"
    val token = "Bearer someToken"
    val input = mapOf(
      "headers" to mapOf("Authorization" to token)
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userService.doesUserExist(username)).thenReturn(false)
    `when`(userService.deleteUser(username)).thenCallRealMethod()

    // Then
    val exception = assertThrows<NotFoundException> {
      deleteUser.handleBusinessLogic(input, context)
    }

    assertEquals("'USER' not found.", exception.message)
  }


  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val token = "invalidToken"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer $token")
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenThrow(ValidationException("Invalid token"))
    val exception = assertThrows<ValidationException> {
      deleteUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid token", exception.message)
  }
}
