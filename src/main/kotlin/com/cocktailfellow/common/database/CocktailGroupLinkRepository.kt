package com.cocktailfellow.common.database

import com.cocktailfellow.common.ValidationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class CocktailGroupLinkRepository {
  companion object {
    private val log: Logger = LogManager.getLogger(CocktailGroupLinkRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val linkTable: String = System.getenv("COCKTAIL_GROUP_LINK_TABLE")
    private const val ID_PATTERN: String = "%s-%s"

    fun createCocktailToGroupLink(groupId: String, cocktailId: String) {
      val cocktailGroupLink = String.format(ID_PATTERN, cocktailId, groupId)

      if (doesLinkAlreadyExist(cocktailGroupLink)) {
        throw ValidationException("The cocktail is already linked to the group.") // todo: refactor
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
        dynamoDb.putItem(putGroupRequest)
        log.info("Linked cocktail '$cocktailId' to group '$groupId'.")
      } catch (e: ConditionalCheckFailedException) {
        log.error("Link between cocktail '$cocktailId' and group '$groupId' already exists.")
        throw throw ValidationException("User is already linked to this group.")
      }
    }

    private fun doesLinkAlreadyExist(link: String): Boolean {
      val itemRequest = GetItemRequest.builder()
        .tableName(linkTable)
        .key(mapOf("id" to AttributeValue.builder().s(link).build()))
        .build()

      val response = dynamoDb.getItem(itemRequest)
      return response.item().isNotEmpty()
    }
  }
}
