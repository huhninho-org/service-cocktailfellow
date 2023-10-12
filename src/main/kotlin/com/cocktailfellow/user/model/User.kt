package com.cocktailfellow.user.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val username: String,
  val hashedPassword: String?
)

@Serializable
data class UserCreateRequest(
  val username: String,
  val password: String
)

@Serializable
data class UserCreate(
  val userId: String,
  val username: String,
  val hashedPassword: String
)

@Serializable
data class LoginRequest(
  val username: String,
  val password: String
)
