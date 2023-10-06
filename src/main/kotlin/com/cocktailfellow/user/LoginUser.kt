package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.token.TokenManagement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest

class LoginUser : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")

  override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = input["body"] as String
    val loginRequest = Json.decodeFromString<LoginRequest>(body)

    val itemRequest = GetItemRequest.builder()
      .tableName(userTableName)
      .key(mapOf("username" to AttributeValue.builder().s(loginRequest.username).build()))
      .build()

    val response = dynamoDb.getItem(itemRequest)
    val item = response.item()

    return if (item != null && BCrypt.checkpw(loginRequest.password, item["password"]?.s())) {
      val loginToken = TokenManagement.createLoginToken(loginRequest.username)
      val refreshToken = TokenManagement.createRefreshToken(loginRequest.username)

      ApiGatewayResponse.build {
        statusCode = HttpStatusCode.OK.code
        objectBody = objectMapper.writeValueAsString(mapOf("loginToken" to loginToken, "refreshToken" to refreshToken))
        headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      }
    } else {
      ApiGatewayResponse.build {
        statusCode = HttpStatusCode.UNAUTHORIZED.code
        headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
      }
    }
  }
}
