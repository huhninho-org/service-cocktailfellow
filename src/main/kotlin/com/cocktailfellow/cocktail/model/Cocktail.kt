package com.cocktailfellow.cocktail.model

import com.cocktailfellow.ingredient.model.Ingredient
import kotlinx.serialization.Serializable

@Serializable
data class Cocktail(
  val cocktailId: String,
  val name: String,
  val method: String?,
  val story: String?,
  val notes: String?,
  val ingredients: List<Ingredient>
)

@Serializable
data class CocktailInfo(
  val cocktailId: String,
  val name: String,
  val method: String?
)

@Serializable
data class CocktailIngredients(
  val cocktailId: String,
  val name: String,
  val ingredients: List<Ingredient>
)
