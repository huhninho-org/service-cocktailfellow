package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.UserBaseTest
import com.cocktailfellow.common.EnvironmentVariables
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JwtTokenException
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.user.common.User
import com.cocktailfellow.user.common.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class LoginUserTestUser : UserBaseTest()  {
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
    EnvironmentVariables["APP_SECRET_KEY"] = "yourSuperStrongSecretKeyHereMakeSureItIsAtLeast32CharactersLongForTestingPurposes"
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
}
