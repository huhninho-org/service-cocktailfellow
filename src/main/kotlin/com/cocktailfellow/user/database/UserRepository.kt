package com.cocktailfellow.user.database

import com.cocktailfellow.common.CreateItemException
import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.UpdateItemException
import com.cocktailfellow.user.model.User
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(UserRepository::class.java)
  private val userTable: String = System.getenv("USER_TABLE")

  fun persistUser(userCreate: User) {
    val item = mapOf(
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
      throw CreateItemException("Username '${userCreate.username}' already exists.")
    } catch (e: Exception) {
      throw CreateItemException("Create '${userCreate.username}' failed.")
    }
  }

  fun getUser(username: String): User {
    val itemRequest = GetItemRequest.builder()
      .tableName(userTable)
      .key(mapOf("username" to AttributeValue.builder().s(username).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    val item = response.item()

    return User(
      username = username,
      hashedPassword = item["password"]?.s()
    )
  }

  fun updateUserPassword(user: User) {
    val item = mapOf(
      "username" to AttributeValue.builder().s(user.username).build(),
      "password" to AttributeValue.builder().s(user.hashedPassword).build()
    )

    val putItemRequest = PutItemRequest.builder()
      .tableName(userTable)
      .item(item)
      .conditionExpression("attribute_exists(username)")
      .build()
    try {
      dynamoDbClient.putItem(putItemRequest)
    } catch (e: ConditionalCheckFailedException) {
      throw UpdateItemException("Username '${user.username}' does not exists.")
    } catch (e: Exception) {
      throw UpdateItemException("Update password of '${user.username}' failed.")
    }
  }

  fun doesUserExist(username: String): Boolean {
    val itemRequest = GetItemRequest.builder()
      .tableName(userTable)
      .key(mapOf("username" to AttributeValue.builder().s(username).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    return response.item().isNotEmpty()
  }

  fun deleteUser(username: String) {
    val keyMap = mapOf(
      "username" to AttributeValue.builder().s(username).build()
    )

    val deleteItemRequest = DeleteItemRequest.builder()
      .tableName(userTable)
      .key(keyMap)
      .build()

    try {
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("User with id '$username' deleted.")
    } catch (e: Exception) {
      log.error("Failed to delete user with id '$username'.")
      throw Exception("Failed to delete user with id '$username'.")
    }
  }
}
