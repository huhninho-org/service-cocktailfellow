package com.cocktailfellow.user.database

import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.user.model.User
import com.cocktailfellow.user.model.UserCreate
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserRepository {
  companion object {
    private val dynamoDb = DynamoDbClient.create()
    private val userTable: String = System.getenv("USER_TABLE")

    fun persistUser(userCreate: UserCreate) {
      val username: String = userCreate.username

      if (usernameAlreadyExists(username))
        throw ValidationException("Username '${username}' already exists.")

      val item = mapOf(
        "userId" to AttributeValue.builder().s(userCreate.userId).build(),
        "username" to AttributeValue.builder().s(username).build(),
        "password" to AttributeValue.builder().s(userCreate.hashedPassword).build()
      )

      val putItemRequest = PutItemRequest.builder()
        .tableName(userTable)
        .item(item)
        .conditionExpression("attribute_not_exists(username)")
        .build()

      try {
        dynamoDb.putItem(putItemRequest)
      } catch (e: ConditionalCheckFailedException) {
        throw ValidationException("Username '${userCreate.username}' already exists.")
      }
    }

    private fun usernameAlreadyExists(username: String): Boolean {
      val queryRequest = QueryRequest.builder()
        .tableName(userTable)
        .indexName("username-index")
        .keyConditionExpression("username = :usernameVal")
        .expressionAttributeValues(mapOf(":usernameVal" to AttributeValue.builder().s(username).build()))
        .build()

      val queryResponse = dynamoDb.query(queryRequest)
      return queryResponse.count() > 0
    }

    fun getUserId(username: String): String {
      val scanRequest = ScanRequest.builder()
        .tableName(userTable)
        .filterExpression("username = :usernameValue")
        .expressionAttributeValues(mapOf(":usernameValue" to AttributeValue.builder().s(username).build()))
        .build()

      val response = dynamoDb.scan(scanRequest)

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

      val response = dynamoDb.getItem(itemRequest)
      val item = response.item()

      return User(
        username = username,
        hashedPassword = item["password"]?.s()
      )
    }
  }
}
