package com.cocktailfellow.cocktail.database

import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class CocktailRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(CocktailRepository::class.java)
  private val cocktailTable: String = System.getenv("COCKTAIL_TABLE")

  fun createCocktail(cocktail: Cocktail) {
    val cocktailJson = JsonConfig.instance.encodeToString(cocktail)
    val item = mapOf(
      "cocktailId" to AttributeValue.builder().s(cocktail.cocktailId).build(),
      "data" to AttributeValue.builder().s(cocktailJson).build()
    )

    val putCocktailRequest = PutItemRequest.builder()
      .tableName(cocktailTable)
      .item(item)
      .build()

    try {
      dynamoDbClient.putItem(putCocktailRequest)
      log.info("Cocktail '${cocktail.name}' created.")
    } catch (e: Exception) {
      log.error("Failed to create cocktail '${cocktail.name}'. Error: ${e.message}")
      throw Exception("Failed to create cocktail '${cocktail.name}'.")
    }
  }

  fun updateCocktail(cocktail: Cocktail) {
    val cocktailJson = JsonConfig.instance.encodeToString(cocktail)

    val updateItemRequest = UpdateItemRequest.builder()
      .tableName(cocktailTable)
      .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktail.cocktailId).build()))
      .updateExpression("SET #data = :data")
      .expressionAttributeNames(mapOf("#data" to "data"))
      .expressionAttributeValues(mapOf(":data" to AttributeValue.builder().s(cocktailJson).build()))
      .conditionExpression("attribute_exists(cocktailId)")
      .build()

    try {
      dynamoDbClient.updateItem(updateItemRequest)
      log.info("Cocktail '${cocktail.name}' updated.")
    } catch (e: Exception) {
      log.error("Failed to update cocktail '${cocktail.name}'. Error: ${e.message}")
      throw Exception("Failed to update cocktail '${cocktail.name}'.")
    }
  }

  fun getCocktail(cocktailId: String): Cocktail {
    val itemRequest = GetItemRequest.builder()
      .tableName(cocktailTable)
      .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
      .build()

    val item = dynamoDbClient.getItem(itemRequest).item()
    val cocktailJson = item["data"]?.s()

    return if (cocktailJson != null) {
      JsonConfig.instance.decodeFromString(cocktailJson)
    } else {
      throw NotFoundException(Type.COCKTAIL)
    }
  }

  fun deleteCocktail(cocktailId: String) {
    val keyMap = mapOf(
      "cocktailId" to AttributeValue.builder().s(cocktailId).build()
    )

    val deleteItemRequest = DeleteItemRequest.builder()
      .tableName(cocktailTable)
      .key(keyMap)
      .build()

    try {
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("Cocktail with id '$cocktailId' deleted.")
    } catch (e: Exception) {
      log.error("Failed to delete cocktail with id '$cocktailId'. error: ${e.message}")
      throw Exception("Failed to delete cocktail with id '$cocktailId'.")
    }
  }

}
