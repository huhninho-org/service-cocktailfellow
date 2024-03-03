package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.JsonConfig
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import com.cocktailfellow.group.model.Group
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class GetGroupsTest : BaseTest() {

  private lateinit var getGroups: GetGroups
  private lateinit var context: Context
  private lateinit var tokenManagement: TokenManagement
  private lateinit var userGroupLinkService: UserGroupLinkService
  private lateinit var groupService: GroupService

  @BeforeEach
  fun setup() {
    context = Mockito.mock(Context::class.java)
    tokenManagement = Mockito.mock(TokenManagement::class.java)
    userGroupLinkService = Mockito.mock(UserGroupLinkService::class.java)
    groupService = Mockito.mock(GroupService::class.java)
    getGroups = GetGroups(tokenManagement, userGroupLinkService, groupService)
  }

  @Test
  fun `test handleBusinessLogic with valid input`() {
    // Given
    val input = mapOf("headers" to mapOf("Authorization" to "Bearer token"))
    val groups = listOf(
      mutableMapOf("groupId" to AttributeValue.builder().s("group1").build()),
      mutableMapOf("groupId" to AttributeValue.builder().s("group2").build())
    )

    val expectedGroups = listOf(
      Group(groupId = "group1", groupName = "Group 1"),
      Group(groupId = "group2", groupName = "Group 2")
    )

    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups("username")).thenReturn(groups)
    `when`(groupService.getGroup("group1")).thenReturn(Group(groupId = "group1", groupName = "Group 1"))
    `when`(groupService.getGroup("group2")).thenReturn(Group(groupId = "group2", groupName = "Group 2"))

    // When
    val response = getGroups.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    val responseObj = response.body?.let { JsonConfig.instance.decodeFromString<GetGroupsFullResponse>(it) }

    val actualGroups = responseObj?.result!!
    assertEquals(expectedGroups, actualGroups.groups)
  }

  @Test
  fun `test handleBusinessLogic with no groups for user`() {
    // Given
    val input = mapOf("headers" to mapOf("Authorization" to "Bearer valid-token"))
    `when`(tokenManagement.validateTokenAndGetData(ArgumentMatchers.any())).thenReturn(
      TokenManagementData("username", "token")
    )
    `when`(userGroupLinkService.getGroups("username")).thenReturn(emptyList())

    // When
    val response = getGroups.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
    val responseObj = response.body?.let { JsonConfig.instance.decodeFromString<GetGroupsFullResponse>(it) }

    val actualGroups = responseObj?.result!!
    assertTrue(actualGroups.groups.isNullOrEmpty(), "Expected groups to be empty but was ${actualGroups.groups}")
  }

  @Test
  fun `test handleBusinessLogic with invalid token`() {
    // Given
    val input = mapOf("headers" to mapOf("Authorization" to "Bearer invalid-token"))
    `when`(tokenManagement.validateTokenAndGetData(any())).thenThrow(ValidationException("Invalid token"))

    // When
    val exception = assertThrows<ValidationException> {
      getGroups.handleBusinessLogic(input, context)
    }

    // Then
    assertEquals("Invalid token", exception.message)
  }

  @Serializable
  data class GetGroupsFullResponse(
    val result: GetGroupsResponse,
    val loginToken: String
  )
}
