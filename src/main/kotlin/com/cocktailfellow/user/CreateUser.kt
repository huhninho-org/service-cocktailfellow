package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class CreateUser : AbstractRequestHandler() {

  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")
  private val rquiredApiKey: String = "V8mjtjn1Kv9TofGELg7ZZL0lHODIlnLl" // todo: replace

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    var headers = input["headers"] as Map<*, *>?
    val apiKey = headers?.get("x-api-key")

    val body = input["body"] as String?
    val user: User

    if (apiKey == null || apiKey != rquiredApiKey) {
      return ApiGatewayResponse.build {
        statusCode = HttpStatusCode.FORBIDDEN.code
        headers = mapOf("Content-Type" to "application/json")
      }
    }

    try {
      user = body?.let { JsonConfig.instance.decodeFromString(it) }
        ?: throw ValidationException("No user found in the request body.")
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON")
    }

    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

    val item = mapOf(
      "username" to AttributeValue.builder().s(user.username).build(),
      "password" to AttributeValue.builder().s(hashedPassword).build(),
      "groups" to AttributeValue.builder().s("").build()
    )

    val putItemRequest = PutItemRequest.builder()
      .tableName(userTableName)
      .item(item)
      .conditionExpression("attribute_not_exists(username)")
      .build()

    try {
      dynamoDb.putItem(putItemRequest)
      LOG.info("User '${user.username}' created.")
    } catch (e: ConditionalCheckFailedException) {
      throw ValidationException("Username '${user.username}' already exists.")
    }

    LOG.info("User '${user.username}' created.")

    return ApiGatewayResponse.build {
      statusCode = HttpStatusCode.CREATED.code
      headers = mapOf("X-Powered-By" to "AWS Lambda & serverless", "Content-Type" to "application/json")
    }
  }

  companion object {
    private val LOG = LogManager.getLogger(CreateUser::class.java)
  }
}
