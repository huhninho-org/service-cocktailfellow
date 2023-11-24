package com.cocktailfellow.common.token

import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.JwtTokenException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenManagementTest : BaseTest() {

  private val tokenManagement = TokenManagement()

  @Test
  fun `test createLoginToken`() {
    // Given
    val username = "testUser"

    // When
    val token = tokenManagement.createLoginToken(username)

    // Then
    assertNotNull(token)
  }

  @Test
  fun `test validateTokenAndGetData with valid token`() {
    // Given
    val username = "testUser"
    val token = tokenManagement.createLoginToken(username)

    // When
    val data = tokenManagement.validateTokenAndGetData("Bearer $token")

    // Then
    assertEquals(username, data.username)
    assertNotNull(data.loginToken)
  }

  @Test
  fun `test validateTokenAndGetData with invalid token`() {
    // Given
    val token = "invalidToken"

    // When Then
    assertThrows<JwtTokenException> {
      tokenManagement.validateTokenAndGetData("Bearer $token")
    }
  }

  @Test
  fun `test validateTokenAndGetData with no token`() {
    // Given
    val token = null

    // When Then
    assertThrows<JwtTokenException> {
      tokenManagement.validateTokenAndGetData("Bearer $token")
    }
  }

  @Test
  fun `test validateTokenAndGetData with token without Bearer`() {
    // Given
    val username = "testUser"
    val token = tokenManagement.createLoginToken(username)

    // When Then
    assertThrows<JwtTokenException> {
      tokenManagement.validateTokenAndGetData(token)
    }
  }
}
