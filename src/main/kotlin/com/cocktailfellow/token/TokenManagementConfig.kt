package com.cocktailfellow.token

object TokenManagementConfig {
  val appSecretKey: String = System.getenv("APP_SECRET_KEY")
  val jwtTtl: Long = System.getenv("JWT_TTL").toLong()
}
