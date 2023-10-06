package com.cocktailfellow.token

import com.cocktailfellow.common.ErrorType
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

  companion object {
    private const val SECRET_KEY = "yourSuperStrongSecretKeyHereMakeSureItIsAtLeast32CharactersLong"
    private val key: Key = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray())
    private val nowMillis = System.currentTimeMillis()
    private val now = Date(nowMillis)
    private var LOG: Logger = LogManager.getLogger(TokenManagement::class.java)

    fun validateToken(token: String?): String? {
      val username = getUsername(token)
      return createLoginToken(username)
    }

    fun getUsername(token: String?): String {
      if (token.isNullOrBlank()) throw JwtTokenException("No token provided", ErrorType.JWT_INVALID_EXCEPTION)
      val bearerToken = token.split(" ")[1]
      val username: String?
      try {
        username = Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(bearerToken)
          .body
          .get("username", String::class.java)
        LOG.info("Valid token for user $username")
      } catch (exception: SignatureException) {
        throw JwtTokenException("Invalid token signature", ErrorType.JWT_INVALID_SIGNATURE_EXCEPTION)
      } catch (exception: ExpiredJwtException) {
        throw JwtTokenException("Token expired", ErrorType.JWT_EXPIRED_EXCEPTION)
      } catch (exception: Exception) {
        throw JwtTokenException("Invalid Token", ErrorType.JWT_INVALID_EXCEPTION)
      }
      return username ?: throw JwtTokenException("Invalid token", ErrorType.JWT_INVALID_EXCEPTION)
    }

    fun createLoginToken(username: String): String? {
      return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(Date(nowMillis + 1800000))  // Valid for 30 minutes
        .claim("username", username)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact()
    }

    fun createRefreshToken(username: String): String? {
      return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(Date(nowMillis + 43200000))  // Valid for 12 hours
        .claim("username", username)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact()
    }
  }
}
