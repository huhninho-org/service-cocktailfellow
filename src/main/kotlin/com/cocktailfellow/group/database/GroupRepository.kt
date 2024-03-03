package com.cocktailfellow.group.database

import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.group.model.Group
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

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

  fun getGroup(groupId: String): Group {
    val itemRequest = GetItemRequest.builder()
      .tableName(groupTable)
      .key(mapOf("groupId" to AttributeValue.builder().s(groupId).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    val groupItem = response.item()

    val groupName = groupItem["groupname"]?.s() ?: throw ValidationException("Group name is missing")
    val isProtected = groupItem["isProtected"]?.bool() ?: false

    return Group(
      groupId = groupId,
      groupName = groupName,
      isProtected = isProtected
    )
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

    val conditionExpression = "attribute_not_exists(isProtected) OR isProtected = :falseValue"

    val expressionAttributeValues = mapOf(
      ":falseValue" to AttributeValue.builder().bool(false).build()
    )

    val deleteItemRequest = DeleteItemRequest.builder()
      .tableName(groupTable)
      .key(keyMap)
      .conditionExpression(conditionExpression)
      .expressionAttributeValues(expressionAttributeValues)
      .build()

    try {
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("Group with id '$groupId' deleted.")
    } catch (e: DynamoDbException) {
      if (e.statusCode() == 400 && e.awsErrorDetails().errorCode() == "ConditionalCheckFailedException") {
        log.info("Deletion skipped for protected group with id '$groupId'.")
      } else {
        log.error(
          "Failed to delete group with id '$groupId'. AWS error code: ${
            e.awsErrorDetails().errorCode()
          }, Message: ${e.message}"
        )
        throw e
      }
    } catch (e: Exception) {
      log.error("Failed to delete group with id '$groupId'. error: ${e.message}")
      throw Exception("Failed to delete group with id '$groupId'.")
    }
  }
}

