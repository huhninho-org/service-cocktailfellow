package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.UserBaseTest
import com.cocktailfellow.common.*
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class LoginUserTestUser : UserBaseTest() {
  private lateinit var loginUser: LoginUser
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userService: UserService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    userService = Mockito.mock(UserService::class.java)
    loginUser = LoginUser(tokenManagement, userService)
  }

  @Test
  fun `test LoginRequest username property`() {
    // Given
    val username = "testUser"

    // When
    val loginRequest = LoginRequest(username, "testPassword")

    // Then
    assertEquals(username, loginRequest.username)
  }

  @Test
  fun `test LoginRequest password property`() {
    // Given
    val password = "testPassword"

    // When
    val loginRequest = LoginRequest("testUser", password)

    // Then
    assertEquals(password, loginRequest.password)
  }

  @Test
  fun `test login user works as expected`() {
    // Given
    EnvironmentVariables["APP_SECRET_KEY"] =
      "yourSuperStrongSecretKeyHereMakeSureItIsAtLeast32CharactersLongForTestingPurposes"
    EnvironmentVariables["JWT_TTL"] = "1800000"
    val username = "testUser"
    val password = "testPassword"
    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
    val user = User(username, hashedPassword)
    val input = mapOf(
      "body" to "{\"username\":\"$username\",\"password\":\"$password\"}",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    `when`(tokenManagement.createLoginToken(any())).thenReturn("token")
    `when`(userService.getUser(any())).thenReturn(user)
    val response = loginUser.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
  }


  @Test
  fun `test handleBusinessLogic with invalid API key`() {
    // Given
    val input = mapOf(
      "body" to "{\"username\":\"testUser\",\"password\":\"testPassword\"}",
      "headers" to mapOf("x-api-key" to "wrong-api-key")
    )

    // When
    val exception = assertThrows<JwtTokenException> {
      loginUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals(HttpStatusCode.FORBIDDEN, exception.statusCode)
  }

  @Test
  fun `test handleBusinessLogic with invalid password`() {
    // Given
    val username = "testUser"
    val password = "wrongPassword"
    val hashedPassword = BCrypt.hashpw("testPassword", BCrypt.gensalt())
    val user = User(username, hashedPassword)
    val input = mapOf(
      "body" to "{\"username\":\"$username\",\"password\":\"$password\"}",
      "headers" to mapOf("x-api-key" to "your-api-key")
    )

    // When
    `when`(userService.getUser(any())).thenReturn(user)
    val exception = assertThrows<JwtTokenException> {
      loginUser.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals(HttpStatusCode.UNAUTHORIZED, exception.statusCode)
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
      loginUser.handleBusinessLogic(input, context)
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
