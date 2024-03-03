package com.cocktailfellow.group

import com.cocktailfellow.group.database.GroupRepository
import com.cocktailfellow.group.model.Group
import java.util.*

class GroupService {
  private val groupRepository: GroupRepository = GroupRepository()

  fun createGroup(groupId: String, groupName: String) {
    return groupRepository.createGroup(groupId, groupName)
  }

  fun isProtected(groupId: String): Boolean {
    return groupRepository.getGroup(groupId).isProtected ?: false
  }

  fun getGroup(groupId: String): Group {
    return groupRepository.getGroup(groupId)
  }

  fun doesGroupExist(groupId: String): Boolean {
    return groupRepository.doesGroupExist(groupId)
  }

  fun deleteGroup(groupId: String) {
    groupRepository.deleteGroup(groupId)
  }
}

enum class DefaultGroups {
  DEFAULT_IBA_ID;

  fun toDashedLowerCase(): String {
    return this.name.lowercase(Locale.getDefault()).replace('_', '-')
  }
}
