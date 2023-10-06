package com.cocktailfellow.group.database

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class GroupRepository {
  companion object {
    private var LOG: Logger = LogManager.getLogger(GroupRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val groupTable: String = System.getenv("GROUP_TABLE")

    fun createGroup(groupId: String, groupname: String) {
      val item = mapOf(
        "id" to AttributeValue.builder().s(groupId).build(),
        "groupname" to AttributeValue.builder().s(groupname).build(),
        "isPreferred" to AttributeValue.builder().bool(true).build()
      )

      val putGroupRequest = PutItemRequest.builder()
        .tableName(groupTable)
        .item(item)
        .build()

      dynamoDb.putItem(putGroupRequest)
      LOG.info("Group '${groupname}' created.")
    }
  }
}
