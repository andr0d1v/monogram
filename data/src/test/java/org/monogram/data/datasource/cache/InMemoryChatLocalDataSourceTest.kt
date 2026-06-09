package org.monogram.data.datasource.cache

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.monogram.data.db.model.ChatEntity
import org.monogram.data.db.model.MessageEntity

class InMemoryChatLocalDataSourceTest {
    @Test
    fun `getStartupChats filters zero order sorts and limits`() = runBlocking {
        val source = InMemoryChatLocalDataSource()
        source.insertChats(
            listOf(
                chat(id = 1L, order = 0L, isPinned = true),
                chat(id = 2L, order = 10L, isPinned = false),
                chat(id = 3L, order = 5L, isPinned = true),
                chat(id = 4L, order = 20L, isPinned = false)
            )
        )

        val result = source.getStartupChats(limit = 2)

        assertEquals(listOf(3L, 4L), result.map { it.id })
    }

    @Test
    fun `replaceMessageId removes pending id and keeps final message`() = runBlocking {
        val source = InMemoryChatLocalDataSource()
        val pending = message(id = -10L, chatId = 1L, content = "pending")
        val sent = message(id = 20L, chatId = 1L, content = "sent")

        source.insertMessage(pending)
        source.replaceMessageId(chatId = 1L, oldMessageId = -10L, message = sent)

        val result = source.getLatestMessages(chatId = 1L, limit = 10)

        assertEquals(listOf(20L), result.map { it.id })
        assertEquals("sent", result.single().content)
    }

    private fun chat(id: Long, order: Long, isPinned: Boolean): ChatEntity =
        ChatEntity(
            id = id,
            title = "chat $id",
            unreadCount = 0,
            avatarPath = null,
            lastMessageText = "",
            lastMessageTime = "",
            order = order,
            isPinned = isPinned,
            isMuted = false,
            isChannel = false,
            isGroup = false,
            type = "PRIVATE",
            isArchived = false,
            memberCount = 0,
            onlineCount = 0
        )

    private fun message(id: Long, chatId: Long, content: String): MessageEntity =
        MessageEntity(
            id = id,
            chatId = chatId,
            senderId = 1L,
            senderName = "sender",
            content = content,
            date = 100,
            isOutgoing = true,
            isRead = false
        )
}
