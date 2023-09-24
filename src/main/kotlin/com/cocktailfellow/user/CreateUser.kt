package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.ValidationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class CreateUser : AbstractRequestHandler() {

  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val body = input["body"] as String?
    val user: User
    try {
      user = body?.let { Json.decodeFromString(it) }
        ?: throw ValidationException("No user found in the request body.")
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON.")
    }

    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

    val item = mapOf(
      "username" to AttributeValue.builder().s(user.username).build(),
      "password" to AttributeValue.builder().s(hashedPassword).build()
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
