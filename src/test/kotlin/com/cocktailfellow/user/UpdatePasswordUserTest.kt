package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class UpdatePasswordUserTest {

  private lateinit var updatePasswordUser: UpdatePasswordUser
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userService: UserService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    userService = Mockito.mock(UserService::class.java)
    updatePasswordUser = UpdatePasswordUser(tokenManagement, userService)
  }

  @Test
  fun `test handleBusinessLogic with valid request`() {
    // Given
    val username = "testUser"
    val password = "newPassword"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "body" to "{\"password\":\"$password\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    val response = updatePasswordUser.handleBusinessLogic(input, context)

    // Then
    Mockito.verify(userService).updatePasswordUser(any())
    assertEquals(HttpStatusCode.NO_CONTENT.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer invalidToken"),
      "body" to "{\"password\":\"newPassword\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenThrow(ValidationException("Invalid token"))
    val exception = assertThrows<ValidationException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid token", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with non-existing user`() {
    // Given
    val username = "nonExistingUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer validToken"),
      "body" to "{\"password\":\"newPassword\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )
    `when`(userService.updatePasswordUser(any())).thenThrow(NotFoundException(Type.USER))

    // Then
    val exception = assertThrows<NotFoundException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    assertEquals("'USER' not found.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with empty password`() {
    // Given
    val username = "testUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "body" to "{\"password\":\"\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )

    // Then
    val exception = assertThrows<ValidationException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    assertEquals("Password must be at least 6 characters.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with password too short`() {
    // Given
    val username = "testUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "body" to "{\"password\":\"12345\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )

    // Then
    val exception = assertThrows<ValidationException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    assertEquals("Password must be at least 6 characters.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with invalid body property in request`() {
    // Given
    val username = "testUser"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer token"),
      "body" to "{\"NOT-password\":\"someRandomPassword\"}"
    )

    // When
    `when`(tokenManagement.validateTokenAndGetData(any())).thenReturn(
      TokenManagementData(username, "token")
    )

    // Then
    val exception = assertThrows<ValidationException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    assertEquals("Invalid JSON body.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with invalid JSON in request body`() {
    // Given
    val input = mapOf(
      "headers" to mapOf("x-api-key" to "your-api-key"),
      "body" to "no json object"
    )

    // When
    val exception = assertThrows<ValidationException> {
      updatePasswordUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid JSON body.", exception.message)
  }
}
