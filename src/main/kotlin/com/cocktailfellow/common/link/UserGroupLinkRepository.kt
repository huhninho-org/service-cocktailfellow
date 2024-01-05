package com.cocktailfellow.common.link

import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.LinkException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserGroupLinkRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(UserGroupLinkRepository::class.java)
  private val linkTable: String = System.getenv("USER_GROUP_LINK_TABLE")

  fun createUserToGroupLink(userGroupLink: String, username: String, groupId: String) {
    val item = mapOf(
      "id" to AttributeValue.builder().s(userGroupLink).build(),
      "username" to AttributeValue.builder().s(username).build(),
      "groupId" to AttributeValue.builder().s(groupId).build()
    )

    val putGroupRequest = PutItemRequest.builder()
      .tableName(linkTable)
      .item(item)
      .conditionExpression("attribute_not_exists(id)")
      .build()

    try {
      dynamoDbClient.putItem(putGroupRequest)
      log.info("Linked user '$username' to group '$groupId'.")
    } catch (e: ConditionalCheckFailedException) {
      log.error("Link between user '$username' and group '$groupId' already exists.")
      throw LinkException("User is already linked to this group.")
    } catch (e: Exception) {
      log.error("Linking user '$username' to group '$groupId' failed. ${e.message}")
      throw LinkException("Linking user to group failed.")
    }
  }

  fun deleteUserToGroupLink(userGroupLink: String) {
    val keyMap = mapOf(
      "id" to AttributeValue.builder().s(userGroupLink).build()
    )

    val itemRequest = DeleteItemRequest.builder()
      .tableName(linkTable)
      .key(keyMap)
      .build()

    try {
      dynamoDbClient.deleteItem(itemRequest)
    } catch (e: Exception) {
      throw LinkException(
        "Remove link '$userGroupLink' failed.",
        HttpStatusCode.INTERNAL_SERVER_ERROR
      )
    }
  }

  fun getGroups(username: String): List<MutableMap<String, AttributeValue>> {
    val scanRequest = ScanRequest.builder()
      .tableName(linkTable)
      .filterExpression("username = :usernameValue")
      .expressionAttributeValues(mapOf(":usernameValue" to AttributeValue.builder().s(username).build()))
      .build()

    return dynamoDbClient.scan(scanRequest).items() ?: emptyList()
  }

  fun deleteAllLinksForGroup(groupId: String) {
    val scanRequest = ScanRequest.builder()
      .tableName(linkTable)
      .filterExpression("groupId = :groupIdVal")
      .expressionAttributeValues(mapOf(":groupIdVal" to AttributeValue.builder().s(groupId).build()))
      .build()

    deleteDataset(dynamoDbClient.scan(scanRequest))
  }

  fun deleteAllLinksForUser(username: String) {
    val scanRequest = ScanRequest.builder()
      .tableName(linkTable)
      .filterExpression("username = :usernameVal")
      .expressionAttributeValues(mapOf(":usernameVal" to AttributeValue.builder().s(username).build()))
      .build()

    deleteDataset(dynamoDbClient.scan(scanRequest))
  }

  fun isMemberOfGroup(userGroupLink: String): Boolean {
    return doesLinkAlreadyExist(userGroupLink)
  }

  fun doesLinkAlreadyExist(link: String): Boolean {
    val itemRequest = GetItemRequest.builder()
      .tableName(linkTable)
      .key(mapOf("id" to AttributeValue.builder().s(link).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    return response.item().isNotEmpty()
  }

  private fun deleteDataset(scanResponse: ScanResponse) {
    for (item in scanResponse.items()) {
      val linkId = item["id"]?.s()
      if (linkId != null) {
        val deleteRequest = DeleteItemRequest.builder()
          .tableName(linkTable)
          .key(mapOf("id" to AttributeValue.builder().s(linkId).build()))
          .build()

        try {
          dynamoDbClient.deleteItem(deleteRequest)
          log.info("Link with id '$linkId' deleted.")
        } catch (e: Exception) {
          log.error("Failed to delete link with id '$linkId'. error: ${e.message}")
          throw LinkException("Failed to delete link with id '$linkId'.", HttpStatusCode.INTERNAL_SERVER_ERROR)
        }
      }
    }
  }
}
