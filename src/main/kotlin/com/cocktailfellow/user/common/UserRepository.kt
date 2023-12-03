package com.cocktailfellow.user.common

import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.ValidationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(UserRepository::class.java)
  private val userTable: String = System.getenv("USER_TABLE")

  fun persistUser(userCreate: UserCreate) {
    val item = mapOf(
      "userId" to AttributeValue.builder().s(userCreate.userId).build(),
      "username" to AttributeValue.builder().s(userCreate.username).build(),
      "password" to AttributeValue.builder().s(userCreate.hashedPassword).build()
    )

    val putItemRequest = PutItemRequest.builder()
      .tableName(userTable)
      .item(item)
      .conditionExpression("attribute_not_exists(username)")
      .build()

    try {
      dynamoDbClient.putItem(putItemRequest)
    } catch (e: ConditionalCheckFailedException) {
      throw ValidationException("Username '${userCreate.username}' already exists.")
    }
  }

  fun usernameAlreadyExists(username: String): Boolean {
    val queryRequest = QueryRequest.builder()
      .tableName(userTable)
      .indexName("username-index")
      .keyConditionExpression("username = :usernameVal")
      .expressionAttributeValues(mapOf(":usernameVal" to AttributeValue.builder().s(username).build()))
      .build()

    val queryResponse = dynamoDbClient.query(queryRequest)
    return queryResponse.count() > 0
  }

  fun getUserId(username: String): String {
    val scanRequest = ScanRequest.builder()
      .tableName(userTable)
      .filterExpression("username = :usernameValue")
      .expressionAttributeValues(mapOf(":usernameValue" to AttributeValue.builder().s(username).build()))
      .build()

    val response = dynamoDbClient.scan(scanRequest)

    val items = response.items()
    if (items.isEmpty()) {
      throw ValidationException("No user found for username: $username")
    }

    val userItem = items[0]
    return userItem["userId"]?.s()
      ?: throw ValidationException("No user id found for username: $username.") // todo: refactor exception
  }

  fun getUser(username: String): User {
    val userId: String = getUserId(username)

    val itemRequest = GetItemRequest.builder()
      .tableName(userTable)
      .key(mapOf("userId" to AttributeValue.builder().s(userId).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    val item = response.item()

    return User(
      username = username,
      hashedPassword = item["password"]?.s()
    )
  }

  fun doesUserExist(username: String): Boolean {
    val userId: String = getUserId(username)

    val itemRequest = GetItemRequest.builder()
      .tableName(userTable)
      .key(mapOf("userId" to AttributeValue.builder().s(userId).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    return response.item().isNotEmpty()
  }

  fun deleteUser(username: String) {
    val userId = getUserId(username)

    val keyMap = mapOf(
      "userId" to AttributeValue.builder().s(userId).build()
    )

    val deleteItemRequest = DeleteItemRequest.builder()
      .tableName(userTable)
      .key(keyMap)
      .build()

    try {
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("User with id '$userId' deleted.")
    } catch (e: Exception) {
      throw ValidationException("Failed to delete user with ID '$userId'.") // todo: refactor exception
    }
  }
}
