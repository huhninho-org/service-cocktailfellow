package com.cocktailfellow.cocktail.database

import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.cocktail.model.Ingredient
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class CocktailRepository {
  companion object {
    private val log: Logger = LogManager.getLogger(CocktailRepository::class.java)

    private val dynamoDb = DynamoDbClient.create()
    private val cocktailTable: String = System.getenv("COCKTAIL_TABLE")

    fun createCocktail(
      cocktailId: String,
      name: String,
      method: String?,
      story: String?,
      notes: String?,
      ingredients: List<Ingredient>
    ) {
      val ingredientJson = JsonConfig.instance.encodeToString(ingredients)
      val item = mutableMapOf<String, AttributeValue>(
        "cocktailId" to AttributeValue.builder().s(cocktailId).build(),
        "name" to AttributeValue.builder().s(name).build(),
        "ingredients" to AttributeValue.builder().s(ingredientJson).build()
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

    fun getCocktail(cocktailId: String): Cocktail {
      val itemRequest = GetItemRequest.builder()
        .tableName(cocktailTable)
        .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
        .build()

      val item = dynamoDb.getItem(itemRequest).item()

      val ingredientJson = item["ingredients"]?.s()
      val ingredients = ingredientJson?.let {
        JsonConfig.instance.decodeFromString<List<Ingredient>>(it)
      } ?: emptyList()

      return Cocktail(
        cocktailId = item["cocktailId"]?.s()!!,
        name = item["name"]?.s()!!,
        method = item["method"]?.s(),
        story = item["story"]?.s(),
        notes = item["notes"]?.s(),
        ingredients = ingredients,
      )
    }

    fun getCocktailInfo(cocktailId: String): CocktailInfo {
      val projectionExpression = "#ci, #n, #m"

      val expressionAttributeNames = mapOf(
        "#ci" to "cocktailId",
        "#n" to "name",
        "#m" to "method"
      )

      val itemRequest = GetItemRequest.builder()
        .tableName(cocktailTable)
        .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
        .projectionExpression(projectionExpression)
        .expressionAttributeNames(expressionAttributeNames)
        .build()

      val item = dynamoDb.getItem(itemRequest).item()

      return CocktailInfo(
        cocktailId = item["cocktailId"]?.s()!!,
        name = item["name"]?.s()!!,
        method = item["method"]?.s()
      )
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

