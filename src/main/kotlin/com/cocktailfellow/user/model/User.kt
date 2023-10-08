package com.cocktailfellow.user.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
  val username: String,
  val password: String
)

@Serializable
data class LoginRequest(val username: String, val password: String)
