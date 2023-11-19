package com.cocktailfellow

import com.cocktailfellow.common.EnvironmentVariables
import org.junit.jupiter.api.BeforeAll

open class UserBaseTest {
  companion object {
    @JvmStatic
    @BeforeAll
    fun setUpEnvironmentVariables() {
      EnvironmentVariables["APP_API_KEY"] = "your-api-key"
    }
  }
}
