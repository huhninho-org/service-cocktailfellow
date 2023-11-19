package com.cocktailfellow.user.common

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val username: String,
  val hashedPassword: String?
)

@Serializable
data class UserCreate(
  val userId: String,
  val username: String,
  val hashedPassword: String
)
