package com.cocktailfellow.group

import com.amazonaws.services.lambda.runtime.Context
import com.cocktailfellow.BaseTest
import com.cocktailfellow.common.BadRequestException
import com.cocktailfellow.common.HttpStatusCode
import com.cocktailfellow.common.link.UserGroupLinkService
import com.cocktailfellow.common.token.TokenManagement
import com.cocktailfellow.common.token.TokenManagementData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

class DeleteGroupTest : BaseTest() {

  private lateinit var deleteGroup: DeleteGroup
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
    deleteGroup = DeleteGroup(tokenManagement, userGroupLinkService, groupService)
  }

  @Test
  fun `test group is deleted successfully`() {
    // Given
    val groupId = "group-id"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer valid-token"),
      "pathParameters" to mapOf("groupId" to groupId)
    )
    Mockito.`when`(tokenManagement.validateTokenAndGetData(Mockito.anyString())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup(Mockito.anyString(), Mockito.anyString())).thenReturn(true)

    // When
    val response = deleteGroup.handleBusinessLogic(input, context)

    // Then
    assertEquals(HttpStatusCode.OK.code, response.statusCode)
  }

  @Test
  fun `test deletion attempt by non-member`() {
    // Given
    val groupId = "group-id"
    val input = mapOf(
      "headers" to mapOf("Authorization" to "Bearer valid-token"),
      "pathParameters" to mapOf("groupId" to groupId)
    )
    Mockito.`when`(tokenManagement.validateTokenAndGetData(Mockito.anyString())).thenReturn(
      TokenManagementData("username", "token")
    )
    Mockito.`when`(userGroupLinkService.isMemberOfGroup(Mockito.anyString(), Mockito.anyString())).thenReturn(false)

    // When/Then
    val exception = assertThrows<BadRequestException> {
      deleteGroup.handleBusinessLogic(input, context)
    }
    assertEquals("User is not member of the given group.", exception.message)
  }
}
