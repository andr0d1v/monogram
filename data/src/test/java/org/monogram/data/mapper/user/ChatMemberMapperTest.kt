package org.monogram.data.mapper.user

import org.drinkless.tdlib.TdApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.monogram.domain.models.UserModel
import org.monogram.domain.repository.ChatMemberStatus

class ChatMemberMapperTest {

    @Test
    fun `toDomain maps trimmed tag to rank`() {
        val member = TdApi.ChatMember().apply {
            tag = "  Builders  "
            status = TdApi.ChatMemberStatusMember()
        }

        val result = member.toDomain(user = createUser())

        assertEquals("Builders", result.rank)
    }

    @Test
    fun `toDomain maps blank tag to null rank`() {
        val member = TdApi.ChatMember().apply {
            tag = "   "
            status = TdApi.ChatMemberStatusMember()
        }

        val result = member.toDomain(user = createUser())

        assertNull(result.rank)
    }

    @Test
    fun `toDomain preserves administrator status for differentiated UI`() {
        val member = TdApi.ChatMember().apply {
            tag = "Moderator"
            status = TdApi.ChatMemberStatusAdministrator(
                true,
                TdApi.ChatAdministratorRights(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false
                )
            )
        }

        val result = member.toDomain(user = createUser())

        assertTrue(result.status is ChatMemberStatus.Administrator)
    }

    private fun createUser() = UserModel(
        id = 1L,
        firstName = "Test",
        lastName = "User"
    )
}
