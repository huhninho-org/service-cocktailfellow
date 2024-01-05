package com.cocktailfellow.common.token

import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.JwtTokenException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

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
  fun `test validateTokenAndGetData with expired token`() {
    // Given
    val token = createLoginTokenCustom(jjtTtl = -1)

    // When Then
    assertThrows<JwtTokenException> {
      tokenManagement.validateTokenAndGetData("Bearer $token")
    }
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

  private fun createLoginTokenCustom(
    username: String = "username",
    nowMillis: Long = System.currentTimeMillis(),
    jjtTtl: Long = 1,
    issuedAd: Date = Date(nowMillis),
    key: String = "default-key-string-111111111112222222222223333333333311111111111222222222222333333333331111111111122222222222233333333333"
  ): String {
    val hmacKey = Keys.hmacShaKeyFor(key.toByteArray())
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(issuedAd)
      .setExpiration(Date(nowMillis + jjtTtl))
      .claim("username", username)
      .signWith(hmacKey, SignatureAlgorithm.HS256)
      .compact()
  }
}
