package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.UserBaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat

class CreateUserTestUser : UserBaseTest() {

  private lateinit var createUser: CreateUser
  private lateinit var context: Context
  private lateinit var userService: UserService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    userService = Mockito.mock(UserService::class.java)
    createUser = CreateUser(userService)
  }

  @Test
  fun `test user is created`() {
    // Given
    val input = mapOf(
      "body" to "{\"username\":\"testUser\",\"password\":\"testPassword\"}",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    val response = createUser.handleBusinessLogic(input, context)

    // Then
    verify(userService).persistUser(argThat {
      username == "testUser" &&
          hashedPassword != "testPassword"
    })
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with no user in request body`() {
    // Given
    val input = mapOf(
      "body" to "{}",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    val exception = assertThrows<ValidationException> {
      createUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Missing body parameter.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with invalid JSON in request body`() {
    // Given
    val input = mapOf(
      "body" to "invalid json",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    val exception = assertThrows<ValidationException> {
      createUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid JSON body.", exception.message)
  }

  @Test
  fun `test handleBusinessLogic with valid request`() {
    // Given
    val input = mapOf(
      "body" to "{\"username\":\"testUser\",\"password\":\"testPassword\"}",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    val response = createUser.handleBusinessLogic(input, context)

    // Then
    verify(userService).persistUser(argThat {
      username == "testUser" &&
          hashedPassword != "testPassword"
    })
    assertEquals(HttpStatusCode.CREATED.code, response.statusCode)
  }
}
