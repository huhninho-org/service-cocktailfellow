package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JwtTokenException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagementDeprecated
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
  private lateinit var userService: UserService
  private lateinit var userGroupLinkService: UserGroupLinkService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    userService = Mockito.mock(UserService::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    deleteUser = DeleteUser(userService, userGroupLinkService)
  }

  @Test
  fun `test handleBusinessLogic with valid request`() {
    // Given
    val username = "testUser"
    val token = TokenManagementDeprecated.createLoginToken(username)
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer $token")
    )

    // When
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
    val token = TokenManagementDeprecated.createLoginToken(username)
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer $token")
    )

    // When
    `when`(userService.doesUserExist(any())).thenReturn(false)
    `when`(userService.deleteUser(any())).thenThrow(ValidationException("The specified user does not exist."))
    val exception = assertThrows<ValidationException> {
      deleteUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals(HttpStatusCode.BAD_REQUEST, exception.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val token = "invalidToken"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer $token")
    )

    // When
    val exception = assertThrows<JwtTokenException> {
      deleteUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals(HttpStatusCode.UNAUTHORIZED, exception.statusCode)
  }
}
