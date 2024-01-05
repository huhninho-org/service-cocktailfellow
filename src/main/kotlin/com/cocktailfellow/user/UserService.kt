package com.cocktailfellow.user

import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.model.User

class UserService {
  private val userRepository: UserRepository = UserRepository()

  fun persistUser(userCreate: User) {
    return userRepository.persistUser(userCreate)
  }

  fun getUser(username: String): User {
    validateUser(username)
    return userRepository.getUser(username)
  }

  fun doesUserExist(username: String): Boolean {
    return userRepository.doesUserExist(username)
  }

  fun deleteUser(username: String) {
    validateUser(username)
    return userRepository.deleteUser(username)
  }

  private fun validateUser(username: String) {
    if (!doesUserExist(username)) {
      throw NotFoundException(Type.USER)
    }
  }
}
