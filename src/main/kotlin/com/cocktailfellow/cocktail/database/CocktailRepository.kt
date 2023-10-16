package com.cocktailfellow.cocktail.database

import com.cocktailfellow.cocktail.Ingredient
import com.cocktailfellow.common.ValidationException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class CocktailRepository {
  companion object {
    private val log: Logger = LogManager.getLogger(CocktailRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val cocktailTable: String = System.getenv("COCKTAIL_TABLE")

    fun createCocktail(cocktailId: String, name: String, method: String?, story: String?, notes: String?, ingredients: List<Ingredient>) {
      val ingredientStrings = ingredients.map { "${it.ingredientName}: ${it.amount}" }
      val item = mutableMapOf<String, AttributeValue>(
        "cocktailId" to AttributeValue.builder().s(cocktailId).build(),
        "name" to AttributeValue.builder().s(name).build(),
        "ingredients" to AttributeValue.builder().l(ingredientStrings.map { AttributeValue.builder().s(it).build() }).build()
      )

      method?.let { item["method"] = AttributeValue.builder().s(it).build() }
      story?.let { item["story"] = AttributeValue.builder().s(it).build() }
      notes?.let { item["notes"] = AttributeValue.builder().s(it).build() }

      val putCocktailRequest = PutItemRequest.builder()
        .tableName(cocktailTable)
        .item(item)
        .build()

      dynamoDb.putItem(putCocktailRequest)
      log.info("Cocktail '${name}' created.")
    }

    fun getCocktail(cocktailId: String): Map<String, AttributeValue> {
      val itemRequest = GetItemRequest.builder()
        .tableName(cocktailTable)
        .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
        .build()

      val response = dynamoDb.getItem(itemRequest)
      return response.item()
    }

    fun doesCocktailExist(cocktailId: String): Boolean {
      val request = GetItemRequest.builder()
        .tableName(cocktailTable)
        .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
        .build()

      val response = dynamoDb.getItem(request)
      return response.item().isNotEmpty()
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
        dynamoDb.deleteItem(deleteItemRequest)
        log.info("Cocktail with id '$cocktailId' deleted.")
      } catch (e: Exception) {
        throw ValidationException("Failed to delete cocktail with id '$cocktailId'.") // todo: refactor exception
      }
    }
  }
}

