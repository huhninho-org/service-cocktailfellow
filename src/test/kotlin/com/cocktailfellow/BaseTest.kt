package com.cocktailfellow

import com.cocktailfellow.common.EnvironmentVariables
import org.junit.jupiter.api.BeforeAll

open class BaseTest {
  companion object {
    @JvmStatic
    @BeforeAll
    fun setUpEnvironmentVariables() {
      EnvironmentVariables["APP_SECRET_KEY"] = "my-app-secret-key"
      EnvironmentVariables["APP_SECRET_KEY"] = "yourSuperStrongSecretKeyHereMakeSureItIsAtLeast32CharactersLongForTestingPurposes"
      EnvironmentVariables["JWT_TTL"] = "1800000"
    }
  }
}
