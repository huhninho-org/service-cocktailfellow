package com.cocktailfellow.common.link

import com.cocktailfellow.common.LinkException
import com.cocktailfellow.user.UserService
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class UserGroupLinkService {
  private val userGroupLinkRepository: UserGroupLinkRepository = UserGroupLinkRepository()
  private val userService: UserService = UserService()
  private val ID_PATTERN: String = "%s-%s"

  fun deleteAllUserGroupLinks(userId: String) {
    return userGroupLinkRepository.deleteAllLinksForUser(userId)
  }

  fun deleteAllLinksForGroup(groupId: String) {
    return userGroupLinkRepository.deleteAllLinksForGroup(groupId)
  }

  fun isMemberOfGroup(username: String, groupId: String): Boolean {
    val userId = userService.getUserId(username)
    val userGroupLink = String.format(ID_PATTERN, userId, groupId)
    return userGroupLinkRepository.isMemberOfGroup(userGroupLink)
  }

  fun createUserToGroupLink(username: String, groupId: String) {
    val userId = userService.getUserId(username)
    val userGroupLink = String.format(ID_PATTERN, userId, groupId)

    if (userGroupLinkRepository.doesLinkAlreadyExist(userGroupLink)) {
      throw LinkException("The user is already linked to the group.")
    }
    return userGroupLinkRepository.createUserToGroupLink(userGroupLink, userId, groupId)
  }

  fun deleteUserToGroupLink(username: String, groupId: String) {
    val userId = userService.getUserId(username)
    val userGroupLink = String.format(ID_PATTERN, userId, groupId)
    return userGroupLinkRepository.deleteUserToGroupLink(userGroupLink)
  }

  fun getGroups(username: String): List<MutableMap<String, AttributeValue>> {
    val userId = userService.getUserId(username)
    return userGroupLinkRepository.getGroups(userId)
  }
}
