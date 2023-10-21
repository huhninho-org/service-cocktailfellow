package com.cocktailfellow.common.database

import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.user.database.UserRepository
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class UserGroupLinkRepository {
  companion object {
    private val log: Logger = LogManager.getLogger(UserGroupLinkRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val linkTable: String = System.getenv("USER_GROUP_LINK_TABLE")
    private const val ID_PATTERN: String = "%s-%s"

    fun createUserToGroupLink(username: String, groupId: String) {
      val userId = UserRepository.getUserId(username)
      val userGroupLink = String.format(ID_PATTERN, userId, groupId)

      if (doesLinkAlreadyExist(userGroupLink)) {
        throw ValidationException("The user is already linked to the group.") // todo: refactor
      }

      val item = mapOf(
        "id" to AttributeValue.builder().s(userGroupLink).build(),
        "userId" to AttributeValue.builder().s(userId).build(),
        "groupId" to AttributeValue.builder().s(groupId).build()
      )

      val putGroupRequest = PutItemRequest.builder()
        .tableName(linkTable)
        .item(item)
        .conditionExpression("attribute_not_exists(id)")
        .build()

      try {
        dynamoDb.putItem(putGroupRequest)
        log.info("Linked user '$userId' to group '$groupId'.")
      } catch (e: ConditionalCheckFailedException) {
        log.error("Link between user '$userId' and group '$groupId' already exists.")
        throw throw ValidationException("User is already linked to this group.")
      }
    }

    fun deleteUserToGroupLink(username: String, groupId: String) {
      val userId = UserRepository.getUserId(username)
      val userGroupLink = String.format(ID_PATTERN, userId, groupId)
      val keyMap = mapOf(
        "id" to AttributeValue.builder().s(userGroupLink).build()
      )

      val itemRequest = DeleteItemRequest.builder()
        .tableName(linkTable)
        .key(keyMap)
        .build()

      try {
        dynamoDb.deleteItem(itemRequest)
      } catch (e: Exception) {
        throw ValidationException("Remove link between user '$userId' and group '$groupId' failed.") // todo: refactor
      }
    }

    fun getGroups(username: String): List<Map<String, String>> {

      val userId = UserRepository.getUserId(username)

      val scanRequest = ScanRequest.builder()
        .tableName(linkTable)
        .filterExpression("userId = :userIdValue")
        .expressionAttributeValues(mapOf(":userIdValue" to AttributeValue.builder().s(userId).build()))
        .build()

      val response = dynamoDb.scan(scanRequest)

      val items = response.items() ?: throw ValidationException("No group found for username: $username")

      return items.map { item ->
        val groupId = item["groupId"]?.s() ?: throw ValidationException("GroupId is missing for username: $username")
        val groupName = GroupRepository.getGroupName(groupId)
        mapOf("groupId" to groupId, "groupName" to groupName)
      }
    }

    fun deleteAllLinksForGroup(groupId: String) {
      val scanRequest = ScanRequest.builder()
        .tableName(linkTable)
        .filterExpression("groupId = :groupIdVal")
        .expressionAttributeValues(mapOf(":groupIdVal" to AttributeValue.builder().s(groupId).build()))
        .build()

      deleteDataset(dynamoDb.scan(scanRequest))
    }

    fun deleteAllLinksForUser(userId: String) {
      val scanRequest = ScanRequest.builder()
        .tableName(linkTable)
        .filterExpression("userId = :userIdVal")
        .expressionAttributeValues(mapOf(":userIdVal" to AttributeValue.builder().s(userId).build()))
        .build()

      deleteDataset(dynamoDb.scan(scanRequest))
    }

    fun isMemberOfGroup(username: String, groupId: String): Boolean {
      val userId = UserRepository.getUserId(username)
      val userGroupLink = String.format(ID_PATTERN, userId, groupId)
      return doesLinkAlreadyExist(userGroupLink)
    }

    private fun doesLinkAlreadyExist(link: String): Boolean {
      val itemRequest = GetItemRequest.builder()
        .tableName(linkTable)
        .key(mapOf("id" to AttributeValue.builder().s(link).build()))
        .build()

      val response = dynamoDb.getItem(itemRequest)
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
            dynamoDb.deleteItem(deleteRequest)
          } catch (e: Exception) {
            throw ValidationException("Failed to delete link with id '$linkId'.") // todo: refactor
          }
        }
      }
    }
  }
}
