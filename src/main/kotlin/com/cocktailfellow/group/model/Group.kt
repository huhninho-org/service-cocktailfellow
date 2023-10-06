package com.cocktailfellow.group.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
  val groupId: String
)

@Serializable
data class Groups(
  val groups: MutableList<Group>
)

@Serializable
data class CreateGroupRequest(
  val groupname: String
)


