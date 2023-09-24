package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import java.util.*

class LoginUser : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")
  private val jwtSecret = Base64.getEncoder()
    .encodeToString("yourSuperStrongSecretKeyHereMakeSureItIsAtLeast32CharactersLong".toByteArray()) // todo: replace

  override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = input["body"] as String
    val loginRequest = Json.decodeFromString<LoginRequest>(body)

    val itemRequest = GetItemRequest.builder()
      .tableName(userTableName)
      .key(mapOf("username" to AttributeValue.builder().s(loginRequest.username).build()))
      .build()

    val response = dynamoDb.getItem(itemRequest)
    val item = response.item()

    if (item != null && BCrypt.checkpw(loginRequest.password, item["password"]?.s())) {
      val nowMillis = System.currentTimeMillis()
      val now = Date(nowMillis)

      // Generate Login Token
      val loginToken = Jwts.builder()
        .setSubject(loginRequest.username)
        .setIssuedAt(now)
        .setExpiration(Date(nowMillis + 1800000))  // Valid for 30 minutes
        .signWith(SignatureAlgorithm.HS256, jwtSecret)
        .compact()

      // Generate Refresh Token
      val refreshToken = Jwts.builder()
        .setSubject(loginRequest.username)
        .setIssuedAt(now)
        .setExpiration(Date(nowMillis + 43200000))  // Valid for 12 hours
        .signWith(SignatureAlgorithm.HS256, jwtSecret)
        .compact()

      // You should probably save this refreshToken in your database, associated with the user

      return ApiGatewayResponse.build {
        statusCode = HttpStatusCode.OK.code
        objectBody = objectMapper.writeValueAsString(mapOf("loginToken" to loginToken, "refreshToken" to refreshToken))
        headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      }
    } else {
      return ApiGatewayResponse.build {
        statusCode = HttpStatusCode.UNAUTHORIZED.code
        headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      }
    }
  }
}
