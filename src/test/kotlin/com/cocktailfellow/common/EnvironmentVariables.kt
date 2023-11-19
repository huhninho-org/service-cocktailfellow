package com.cocktailfellow.common

import java.util.*

object EnvironmentVariables {
  private val ENV = System.getenv()
  operator fun set(key: String, value: String) {
     try {
      modifiableEnvironmentMap[key] = value
    } catch (e: NoSuchFieldException) {
      throw RuntimeException("Could not set environment variable $key", e)
    } catch (e: IllegalAccessException) {
      throw RuntimeException("Could not set environment variable $key", e)
    }
  }

  @get:Throws(NoSuchFieldException::class, IllegalAccessException::class)
  private val modifiableEnvironmentMap: MutableMap<String, String>
    get() {
      val clazz: Class<*> = Collections.unmodifiableMap(emptyMap<Any, Any>()).javaClass
      val declaredField = clazz.getDeclaredField("m")
      declaredField.setAccessible(true)
      return declaredField[ENV] as MutableMap<String, String>
    }
}
