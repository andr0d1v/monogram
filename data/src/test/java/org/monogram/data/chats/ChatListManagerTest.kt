package org.monogram.data.chats

import org.drinkless.tdlib.TdApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.monogram.domain.models.ChatModel

class ChatListManagerTest {
    @Test
    fun `rebuildChatList returns unique chat ids in pinned and order sequence`() {
        val cache = ChatCache()
        cache.allChats[1L] = chat(1L)
        cache.allChats[2L] = chat(2L)
        cache.activeListPositions[1L] = position(order = 10L, isPinned = false)
        cache.activeListPositions[2L] = position(order = 20L, isPinned = true)

        val manager = ChatListManager(cache) {}
        val result = manager.rebuildChatList(limit = 10) { chat, order, isPinned ->
            ChatModel(
                id = chat.id,
                title = chat.title,
                unreadCount = 0,
                order = order,
                isPinned = isPinned
            )
        }

        assertEquals(listOf(2L, 1L), result.map { it.id })
    }

    private fun chat(id: Long): TdApi.Chat =
        TdApi.Chat().apply {
            this.id = id
            title = "chat $id"
            type = TdApi.ChatTypePrivate(id)
        }

    private fun position(order: Long, isPinned: Boolean): TdApi.ChatPosition =
        TdApi.ChatPosition(TdApi.ChatListMain(), order, isPinned, null)
}
