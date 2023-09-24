package com.cocktailfellow.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val username: String,
  val password: String
)

@Serializable
data class LoginRequest(val username: String, val password: String)
