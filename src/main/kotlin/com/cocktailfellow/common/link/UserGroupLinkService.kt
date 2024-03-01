package com.cocktailfellow.common.link

import com.cocktailfellow.common.LinkException
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class UserGroupLinkService {
  private val userGroupLinkRepository: UserGroupLinkRepository = UserGroupLinkRepository()
  private val ID_PATTERN: String = "%s-%s"

  fun deleteAllUserGroupLinks(username: String) {
    return userGroupLinkRepository.deleteAllLinksForUser(username)
  }

  fun deleteAllLinksForGroup(groupId: String) {
    return userGroupLinkRepository.deleteAllLinksForGroup(groupId)
  }

  fun isMemberOfGroup(username: String, groupId: String): Boolean {
    val userGroupLink = String.format(ID_PATTERN, username, groupId)
    return userGroupLinkRepository.isMemberOfGroup(userGroupLink)
  }

  fun createUserToGroupLink(username: String, groupId: String) {
    val userGroupLink = String.format(ID_PATTERN, username, groupId)

    if (userGroupLinkRepository.doesLinkAlreadyExist(userGroupLink)) {
      throw LinkException("The user is already linked to the group.")
    }
    return userGroupLinkRepository.createUserToGroupLink(userGroupLink, username, groupId)
  }

  fun deleteUserToGroupLink(username: String, groupId: String) {
    val userGroupLink = String.format(ID_PATTERN, username, groupId)
    return userGroupLinkRepository.deleteUserToGroupLink(userGroupLink)
  }

  fun getGroups(username: String): List<MutableMap<String, AttributeValue>> {
    return userGroupLinkRepository.getGroups(username)
  }

  fun addIbaDefaultGroup(username: String) {
    val groupId = "default-iba-id"
    val userGroupLink = String.format(ID_PATTERN, username, groupId)

    if (userGroupLinkRepository.doesLinkAlreadyExist(userGroupLink)) {
      throw LinkException("The user is already linked to the group.")
    }
    return userGroupLinkRepository.createUserToGroupLink(userGroupLink, username, groupId)
  }
}
