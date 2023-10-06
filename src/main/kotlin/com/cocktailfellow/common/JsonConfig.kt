package com.cocktailfellow.common

import kotlinx.serialization.json.Json

object JsonConfig {
  val instance: Json = Json {
    ignoreUnknownKeys = true
  }
}
