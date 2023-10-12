package com.cocktailfellow.group.database

import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.database.UserGroupLinkRepository
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class GroupRepository {
  companion object {
    private val log: Logger = LogManager.getLogger(GroupRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val groupTable: String = System.getenv("GROUP_TABLE")

    fun createGroup(groupId: String, groupName: String) {
      val item = mapOf(
        "groupId" to AttributeValue.builder().s(groupId).build(),
        "groupname" to AttributeValue.builder().s(groupName).build()
      )

      val putGroupRequest = PutItemRequest.builder()
        .tableName(groupTable)
        .item(item)
        .build()

      dynamoDb.putItem(putGroupRequest)
      log.info("Group '${groupName}' created.")
    }

    fun getGroupName(groupId: String): String {
      val itemRequest = GetItemRequest.builder()
        .tableName(groupTable)
        .key(mapOf("groupId" to AttributeValue.builder().s(groupId).build()))
        .build()

      val response = dynamoDb.getItem(itemRequest)
      val groupItem = response.item()

      val groupName = groupItem["groupname"]?.s()
      return groupName ?: throw ValidationException("No group id found.") // todo: refactor exception
    }

    fun deleteGroup(groupId: String) {
      val keyMap = mapOf(
        "groupId" to AttributeValue.builder().s(groupId).build()
      )

      val deleteItemRequest = DeleteItemRequest.builder()
        .tableName(groupTable)
        .key(keyMap)
        .build()

      try {
        dynamoDb.deleteItem(deleteItemRequest)
        log.info("Group with id '$groupId' deleted.")
      } catch (e: Exception) {
        throw ValidationException("Failed to delete group with ID '$groupId'.") // todo: refactor exception
      }

      UserGroupLinkRepository.deleteAllLinksForGroup(groupId)
    }
  }
}
