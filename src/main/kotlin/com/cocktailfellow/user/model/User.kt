package com.cocktailfellow.user.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val username: String,
  val hashedPassword: String?
)
