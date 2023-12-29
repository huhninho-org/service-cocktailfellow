package com.cocktailfellow.cocktail.database

import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.DynamoDbClientProvider
import com.cocktailfellow.ingredient.model.Ingredient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class CocktailRepository(
  private val dynamoDbClient: DynamoDbClient = DynamoDbClientProvider.get()
) {
  private val log: Logger = LogManager.getLogger(CocktailRepository::class.java)
  private val cocktailTable: String = System.getenv("COCKTAIL_TABLE")

  fun createCocktail(
    cocktailId: String,
    name: String,
    method: String?,
    story: String?,
    notes: String?,
    ingredients: List<Ingredient>
  ) {
    val ingredientStrings = ingredients.map { "${it.ingredientName}:${it.amount}" }
    val item = mutableMapOf<String, AttributeValue>(
      "cocktailId" to AttributeValue.builder().s(cocktailId).build(),
      "name" to AttributeValue.builder().s(name).build(),
      "ingredients" to AttributeValue.builder().ss(ingredientStrings).build()
    )

    method?.let { item["method"] = AttributeValue.builder().s(it).build() }
    story?.let { item["story"] = AttributeValue.builder().s(it).build() }
    notes?.let { item["notes"] = AttributeValue.builder().s(it).build() }

    val putCocktailRequest = PutItemRequest.builder()
      .tableName(cocktailTable)
      .item(item)
      .build()

    dynamoDbClient.putItem(putCocktailRequest)
    log.info("Cocktail '${name}' created.")
  }

  fun getCocktail(cocktailId: String): Cocktail {
    val itemRequest = GetItemRequest.builder()
      .tableName(cocktailTable)
      .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
      .build()

    val item = dynamoDbClient.getItem(itemRequest).item()

    val ingredientStrings = item["ingredients"]?.ss()
    val ingredients = ingredientStrings?.map {
      val parts = it.split(":")
      Ingredient(ingredientName = parts[0], amount = parts[1])
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

    val item = dynamoDbClient.getItem(itemRequest).item()

    return CocktailInfo(
      cocktailId = item["cocktailId"]?.s()!!,
      name = item["name"]?.s()!!,
      method = item["method"]?.s()
    )
  }

  fun getCocktailIngredients(cocktailId: String): CocktailIngredients {
    val projectionExpression = "#ci, #n, #m, #i"

    val expressionAttributeNames = mapOf(
      "#ci" to "cocktailId",
      "#n" to "name",
      "#m" to "method",
      "#i" to "ingredients"
    )

    val itemRequest = GetItemRequest.builder()
      .tableName(cocktailTable)
      .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
      .projectionExpression(projectionExpression)
      .expressionAttributeNames(expressionAttributeNames)
      .build()

    val item = dynamoDbClient.getItem(itemRequest).item()

    val ingredientStrings = item["ingredients"]?.ss()
    val ingredients = ingredientStrings?.map {
      val parts = it.split(":")
      Ingredient(ingredientName = parts[0], amount = parts[1])
    } ?: emptyList()

    return CocktailIngredients(
      cocktailId = item["cocktailId"]?.s()!!,
      name = item["name"]?.s()!!,
      method = item["method"]?.s()!!,
      ingredients = ingredients
    )
  }

  fun doesCocktailExist(cocktailId: String): Boolean {
    val request = GetItemRequest.builder()
      .tableName(cocktailTable)
      .key(mapOf("cocktailId" to AttributeValue.builder().s(cocktailId).build()))
      .build()

    val response = dynamoDbClient.getItem(request)
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
      dynamoDbClient.deleteItem(deleteItemRequest)
      log.info("Cocktail with id '$cocktailId' deleted.")
    } catch (e: Exception) {
      log.error("Failed to delete cocktail with id '$cocktailId'. error: ${e.message}")
      throw Exception("Failed to delete cocktail with id '$cocktailId'.")
    }
  }

}
