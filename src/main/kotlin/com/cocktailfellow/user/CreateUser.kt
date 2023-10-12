package com.cocktailfellow.user

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.AbstractRequestHandler
import com.cocktailfellow.ApiGatewayResponse
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.user.model.CreateUserRequest
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.mindrot.jbcrypt.BCrypt
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.util.*

class CreateUser : AbstractRequestHandler() {

  private var log: Logger = LogManager.getLogger(CreateUser::class.java)

  private val dynamoDb = DynamoDbClient.create()
  private val userTableName: String = System.getenv("USER_TABLE")
  private val rquiredApiKey: String = "V8mjtjn1Kv9TofGELg7ZZL0lHODIlnLl" // todo: replace

  override fun handleBusinessLogic(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    val headers = input["headers"] as Map<*, *>?
    val apiKey = headers?.get("x-api-key")

    val body = input["body"] as String?
    val user: CreateUserRequest

    log.info(body)

    if (apiKey == null || apiKey != rquiredApiKey) return generateError(HttpStatusCode.FORBIDDEN.code, "Forbidden.")

    try {
      user = body?.let { JsonConfig.instance.decodeFromString(it) }
        ?: throw ValidationException("No user found in the request body.")
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON")
    }

    val userId = UUID.randomUUID().toString()

    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

    val item = mapOf(
      "userId" to AttributeValue.builder().s(userId).build(),
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
      log.info("User '${user.username}' created.")
    } catch (e: ConditionalCheckFailedException) {
      throw ValidationException("Username '${user.username}' already exists.")
    }

    return generateResponse(HttpStatusCode.CREATED.code)
  }
}
