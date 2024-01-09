package com.cocktailfellow.ingredient.model

import com.cocktailfellow.common.validation.Validation.DEFAULT_MAX
import kotlinx.serialization.Serializable
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Serializable
data class Ingredient(
  @field:Size(
    max = DEFAULT_MAX, message = "'ingredientName' exceeds the limit of $DEFAULT_MAX characters."
  )
  @field:NotEmpty
  val ingredientName: String,
  @field:Size(
    max = DEFAULT_MAX, message = "'amount' exceeds the limit of $DEFAULT_MAX characters."
  )
  @field:NotEmpty
  val amount: String
)
