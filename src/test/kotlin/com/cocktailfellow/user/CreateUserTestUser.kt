package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.UserBaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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

  @ParameterizedTest
  @MethodSource("invalidLengthTestData")
  fun `test handleBusinessLogic with invalid field lengths`(field: String, value: String, expectedMessage: String) {
    // Given
    val createUserRequest = when (field) {
      "username" -> CreateUserRequest(value, "password")
      "password" -> CreateUserRequest("username", value)
      else -> throw IllegalArgumentException("Invalid field for test")
    }
    val bodyJson = JsonConfig.instance.encodeToString(CreateUserRequest.serializer(), createUserRequest)

    val input = mapOf(
      "headers" to mapOf("x-api-key" to "your-api-key"),
      "body" to bodyJson
    )

    // Then
    val exception = assertThrows<ValidationException> {
      createUser.handleBusinessLogic(input, context)
    }

    assertEquals(expectedMessage, exception.message)
  }

  companion object {
    @JvmStatic
    fun invalidLengthTestData() = listOf(
      Arguments.of("username", "12", "'username' length should be within 3 to 20 characters."),
      Arguments.of("username", "123456789012345678901", "'username' length should be within 3 to 20 characters."),
      Arguments.of("password", "12345", "'password' length should be within 6 to 20 characters."),
      Arguments.of("password", "123456789012345678901", "'password' length should be within 6 to 20 characters.")
    )
  }
}
