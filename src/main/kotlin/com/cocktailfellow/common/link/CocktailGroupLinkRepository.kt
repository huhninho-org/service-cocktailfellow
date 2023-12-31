package com.cocktailfellow.common.link

import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.LinkException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class CocktailGroupLinkRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(CocktailGroupLinkRepository::class.java)

  private val linkTable: String = System.getenv("COCKTAIL_GROUP_LINK_TABLE")
  private val ID_PATTERN: String = "%s-%s"

  fun createCocktailToGroupLink(groupId: String, cocktailId: String) {
    val cocktailGroupLink = String.format(ID_PATTERN, cocktailId, groupId)

    if (doesLinkAlreadyExist(cocktailGroupLink)) {
      throw LinkException("The cocktail is already linked to the group.")
    }

    val item = mapOf(
      "id" to AttributeValue.builder().s(cocktailGroupLink).build(),
      "cocktailId" to AttributeValue.builder().s(cocktailId).build(),
      "groupId" to AttributeValue.builder().s(groupId).build()
    )

    val putGroupRequest = PutItemRequest.builder()
      .tableName(linkTable)
      .item(item)
      .conditionExpression("attribute_not_exists(id)")
      .build()

    try {
      dynamoDbClient.putItem(putGroupRequest)
      log.info("Linked cocktail '$cocktailId' to group '$groupId'.")
    } catch (e: ConditionalCheckFailedException) {
      log.error("Link between cocktail '$cocktailId' and group '$groupId' already exists.")
      throw LinkException("Cocktail is already linked to this group.")
    } catch (e: Exception) {
      log.error("Linking cocktail '$cocktailId' to group '$groupId' failed. ${e.message}")
      throw LinkException("Linking cocktail to group failed.")
    }
  }

  fun isMemberOfGroup(cocktailId: String, groupId: String): Boolean {
    val userGroupLink = String.format(ID_PATTERN, cocktailId, groupId)
    print("DEBUG: userGroupLink: $userGroupLink")
    return doesLinkAlreadyExist(userGroupLink)
  }

  private fun doesLinkAlreadyExist(link: String): Boolean {
    val itemRequest = GetItemRequest.builder()
      .tableName(linkTable)
      .key(mapOf("id" to AttributeValue.builder().s(link).build()))
      .build()

    val response = dynamoDbClient.getItem(itemRequest)
    return response.item().isNotEmpty()
  }

  fun fetchItems(groupId: String): MutableList<MutableMap<String, AttributeValue>> {
    val scanRequest = ScanRequest.builder()
      .tableName(linkTable)
      .filterExpression("groupId = :groupIdValue")
      .expressionAttributeValues(mapOf(":groupIdValue" to AttributeValue.builder().s(groupId).build()))
      .build()

    val response = dynamoDbClient.scan(scanRequest)

    return response.items() ?: mutableListOf()
  }

  fun deleteLink(cocktailId: String, groupId: String) {
    val userGroupLink = String.format(ID_PATTERN, cocktailId, groupId)
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
      log.error("Failed to delete link with id '$userGroupLink'. error: ${e.message}")
      throw LinkException("Failed to delete link with id '$userGroupLink'.", HttpStatusCode.INTERNAL_SERVER_ERROR)
    }
  }
}

