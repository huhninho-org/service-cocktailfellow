package com.cocktailfellow.group.model

import kotlinx.serialization.Serializable

@Serializable
data class Group (
  val groupId: String,
  val groupName: String,
  val isProtected: Boolean? = false
)
