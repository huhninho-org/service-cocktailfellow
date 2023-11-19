package com.cocktailfellow.group

import com.cocktailfellow.group.database.GroupRepository

class GroupService {
  private val groupRepository: GroupRepository = GroupRepository()

  fun createGroup(groupId: String, groupName: String) {
    return groupRepository.createGroup(groupId, groupName)
  }

  fun getGroupName(groupId: String): String {
    return groupRepository.getGroupName(groupId)
  }

  fun doesGroupExist(groupId: String): Boolean {
    return groupRepository.doesGroupExist(groupId)
  }

  fun deleteGroup(groupId: String) {
    groupRepository.deleteGroup(groupId)
  }
}
