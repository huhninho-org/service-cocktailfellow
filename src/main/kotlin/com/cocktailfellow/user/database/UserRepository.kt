package com.cocktailfellow.user.database

import com.cocktailfellow.common.ValidationException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ScanRequest

class UserRepository {
  companion object {
    private val dynamoDb = DynamoDbClient.create()
    private val userTable: String = System.getenv("USER_TABLE")

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
  }
}
