package com.cocktailfellow.common.token

import com.cocktailfellow.common.ErrorType
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JwtTokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.security.Key
import java.util.*

class TokenManagement {

  private val key: Key = Keys.hmacShaKeyFor(TokenManagementConfig.appSecretKey.toByteArray())
  private val nowMillis = System.currentTimeMillis()
  private val now = Date(nowMillis)
  private val log: Logger = LogManager.getLogger(TokenManagementDeprecated::class.java)

  fun validateTokenAndGetData(loginToken: String?): TokenManagementData {
    val bearerLoginToken = extractBearer(loginToken)
    validateToken(bearerLoginToken)
    val username = getUsername(bearerLoginToken)
    log.info("Valid login for token $bearerLoginToken")
    return TokenManagementData(
      username = username,
      loginToken = createLoginToken(username)
    )
  }

  fun createLoginToken(username: String): String {
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(now)
      .setExpiration(Date(nowMillis + TokenManagementConfig.jwtTtl))
      .claim("username", username)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact()
  }

  private fun extractBearer(token: String?): String {
    if (token.isNullOrBlank()) throw JwtTokenException("No token provided", ErrorType.JWT_INVALID_EXCEPTION)
    val parts = token.split(" ")
    if (parts.size < 2) throw JwtTokenException("Invalid token format", ErrorType.JWT_INVALID_EXCEPTION)
    return parts[1]
  }

  private fun getUsername(token: String?): String {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .body
      .get("username", String::class.java)
  }

  private fun validateToken(token: String?) {
    try {
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .body
        .get("username", String::class.java)
    } catch (exception: SignatureException) {
      throw JwtTokenException("Invalid token signature", ErrorType.JWT_INVALID_SIGNATURE_EXCEPTION)
    } catch (exception: ExpiredJwtException) {
      throw JwtTokenException("Token expired", ErrorType.JWT_EXPIRED_EXCEPTION)
    } catch (exception: Exception) {
      throw JwtTokenException("Invalid Token", ErrorType.JWT_INVALID_EXCEPTION, HttpStatusCode.UNAUTHORIZED)
    }
  }
}
