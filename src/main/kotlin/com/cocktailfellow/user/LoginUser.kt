package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.token.TokenManagement
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.model.LoginRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest

class LoginUser : AbstractRequestHandler() {

  private var log: Logger = LogManager.getLogger(LoginUser::class.java)
  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = input["body"] as String
    val loginRequest = JsonConfig.instance.decodeFromString<LoginRequest>(body)
    val username = loginRequest.username

    val userId = UserRepository.getUserId(username)

    val itemRequest = GetItemRequest.builder()
      .tableName(userTableName)
      .key(mapOf("userId" to AttributeValue.builder().s(userId).build()))
      .build()

    val response = dynamoDb.getItem(itemRequest)
    val item = response.item()

    return if (item != null && BCrypt.checkpw(loginRequest.password, item["password"]?.s())) {
      val loginToken = TokenManagement.createLoginToken(username)
      val refreshToken = TokenManagement.createRefreshToken(username)

      val loginResponse = LoginResponse(
        loginToken = loginToken,
        refreshToken = refreshToken
      )

      return generateLoginResponse(HttpStatusCode.OK.code, loginResponse)
    } else {
      generateError(HttpStatusCode.UNAUTHORIZED.code, "Unauthorized")
    }
  }

  private fun generateLoginResponse(status: Int, result: LoginResponse): ApiGatewayResponse {
    val response = CustomApiResponse(result)
    return ApiGatewayResponse.withBody(status, response)
  }
}

@Serializable
data class CustomApiResponse<LoginResponse>(
  val result: LoginResponse,
)

@Serializable
data class LoginResponse(
  val loginToken: String?,
  val refreshToken: String?
)
