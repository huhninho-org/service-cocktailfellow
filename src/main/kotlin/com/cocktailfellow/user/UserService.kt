package com.cocktailfellow.user

import com.cocktailfellow.common.NotFoundException
import com.cocktailfellow.common.Type
import com.cocktailfellow.user.database.UserRepository
import com.cocktailfellow.user.model.User
import org.mindrot.jbcrypt.BCrypt

class UserService {
  private val userRepository: UserRepository = UserRepository()

  fun encryptPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
  }

  fun persistUser(userCreate: User) {
    return userRepository.persistUser(userCreate)
  }

  fun getUser(username: String): User {
    validateExistingUser(username)
    return userRepository.getUser(username)
  }

  fun doesUserExist(username: String): Boolean {
    return userRepository.doesUserExist(username)
  }

  fun deleteUser(username: String) {
    validateExistingUser(username)
    return userRepository.deleteUser(username)
  }

  private fun validateUser(username: String) {
    if (!doesUserExist(username)) {
      throw NotFoundException(Type.USER)
    }
  }
}
