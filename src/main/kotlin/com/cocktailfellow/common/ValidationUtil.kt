package com.cocktailfellow.common

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import javax.validation.Validation
import javax.validation.Validator
import kotlin.reflect.KClass

object ValidationUtil {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @OptIn(InternalSerializationApi::class)
  fun <T : Any> deserializeAndValidate(body: String, clazz: KClass<T>): T {
    val requestObject: T
    try {
      requestObject = JsonConfig.instance.decodeFromString(clazz.serializer(), body)
    } catch (e: Exception) {
      throw ValidationException("Invalid JSON body.")
    }
    validate(requestObject)
    return requestObject
  }

  private fun <T> validate(obj: T) {
    val violations = validator.validate(obj)
    if (violations.isNotEmpty()) {
      val errorMessage = violations.joinToString("; ") { it.message }
      throw ValidationException(errorMessage)
    }
  }

}
