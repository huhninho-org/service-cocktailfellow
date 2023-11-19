package com.cocktailfellow.user.common

import com.cocktailfellow.common.ValidationException

class UserService {
  private val userRepository: UserRepository = UserRepository()

  fun persistUser(userCreate: UserCreate) {
    val username: String = userCreate.username
    if (userRepository.usernameAlreadyExists(username))
      throw ValidationException("Username '${username}' already exists.")

    return userRepository.persistUser(userCreate)
  }

  fun getUserId(username: String): String {
    validateUser(username)
    return userRepository.getUserId(username)
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
      throw ValidationException("The specified user does not exist.") // todo: refactor
    }
  }
}
