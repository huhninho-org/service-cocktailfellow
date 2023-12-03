package com.cocktailfellow.group.database

import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.ValidationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class GroupRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(GroupRepository::class.java)

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

    dynamoDbClient.putItem(putGroupRequest)
    log.info("Group '${groupName}' created.")
  }

  fun getGroupName(groupId: String): String {
    val itemRequest = GetItemRequest.builder()
      .tableName(groupTable)
      .key(mapOf("groupId" to AttributeValue.builder().s(groupId).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    val groupItem = response.item()

    val groupName = groupItem["groupname"]?.s()
    return groupName ?: throw ValidationException("No group id found.") // todo: refactor exception
  }

  fun doesGroupExist(groupId: String): Boolean {
    val request = GetItemRequest.builder()
      .tableName(groupTable)
      .key(mapOf("groupId" to AttributeValue.builder().s(groupId).build()))
      .build()

    val response = dynamoDbClient.getItem(request)
    return response.item().isNotEmpty()
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
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("Group with id '$groupId' deleted.")
    } catch (e: Exception) {
      throw ValidationException("Failed to delete group with ID '$groupId'.") // todo: refactor exception
    }
  }
}

