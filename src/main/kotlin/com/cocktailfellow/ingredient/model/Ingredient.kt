package com.cocktailfellow.ingredient.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
  val ingredientName: String,
  val amount: String
)
