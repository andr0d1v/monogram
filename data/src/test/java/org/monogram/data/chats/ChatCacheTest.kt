package org.monogram.data.chats

import org.drinkless.tdlib.TdApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatCacheTest {
    @Test
    fun `putChat replaces old cached position with incoming authoritative position`() {
        val cache = ChatCache()
        cache.putChat(chat(id = 1L, order = 100L, isPinned = true))

        cache.putChat(chat(id = 1L, order = 10L, isPinned = false))

        val position = cache.getChat(1L)!!.positions.single()
        assertEquals(10L, position.order)
        assertFalse(position.isPinned)
    }

    private fun chat(id: Long, order: Long, isPinned: Boolean): TdApi.Chat =
        TdApi.Chat().apply {
            this.id = id
            title = "chat $id"
            type = TdApi.ChatTypePrivate(id)
            positions = arrayOf(TdApi.ChatPosition(TdApi.ChatListMain(), order, isPinned, null))
        }
}
