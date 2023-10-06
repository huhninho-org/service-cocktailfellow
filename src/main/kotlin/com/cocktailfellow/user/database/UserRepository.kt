package com.cocktailfellow.user.database

import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.group.model.Groups
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserRepository {
  companion object {
    private var LOG: Logger = LogManager.getLogger(UserRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val userTable: String = System.getenv("USER_TABLE")

    fun getGroupsByUsername(username: String): Groups {
      val itemRequest = GetItemRequest.builder()
        .tableName(userTable)
        .key(mapOf("username" to AttributeValue.builder().s(username).build()))
        .build()

      val response = dynamoDb.getItem(itemRequest)
      val userItem = response.item()

      val groupsJson = userItem["groups"]?.s()
      val groups: Groups? = if (!groupsJson.isNullOrBlank()) {
        try {
          JsonConfig.instance.decodeFromString(groupsJson)
        } catch (e: SerializationException) {
          LOG.error("Error deserializing groups JSON: $groupsJson", e)
          null
        }
      } else {
        LOG.warn("Groups attribute is null or blank")
        null
      }
      return groups ?: Groups(mutableListOf())
    }

    fun updateGroups(groups: Groups, username: String) {
      val groupsJson = Json.encodeToString(groups)

      val updateItemRequest = UpdateItemRequest.builder()
        .tableName(userTable)
        .key(mapOf("username" to AttributeValue.builder().s(username).build()))
        .updateExpression("SET groups = :groupsVal")
        .expressionAttributeValues(mapOf(":groupsVal" to AttributeValue.builder().s(groupsJson).build()))
        .returnValues(ReturnValue.UPDATED_NEW)
        .build()

      val updateResponse = dynamoDb.updateItem(updateItemRequest)
      LOG.info("Groups updated to: '${updateResponse.attributes()["groups"]?.s()}'")
    }

  }
}
