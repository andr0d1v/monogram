package org.monogram.presentation.features.chats.conversation.ui.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.monogram.domain.models.MessageContent
import org.monogram.domain.models.MessageModel

class GroupedMessageItemKeyTest {
    @Test
    fun `album key includes message boundaries to avoid duplicate keys`() {
        val firstAlbum = GroupedMessageItem.Album(
            albumId = 42L,
            messages = listOf(
                message(id = 1001L, albumId = 42L),
                message(id = 1002L, albumId = 42L)
            )
        )
        val secondAlbum = GroupedMessageItem.Album(
            albumId = 42L,
            messages = listOf(
                message(id = 2001L, albumId = 42L),
                message(id = 2002L, albumId = 42L)
            )
        )

        assertNotEquals(firstAlbum.lazyItemKey, secondAlbum.lazyItemKey)
    }

    @Test
    fun `album key is stable for the same grouped item`() {
        val album = GroupedMessageItem.Album(
            albumId = 7L,
            messages = listOf(
                message(id = 3001L, albumId = 7L),
                message(id = 3002L, albumId = 7L)
            )
        )

        assertEquals(album.lazyItemKey, album.lazyItemKey)
    }

    @Test
    fun `single item key remains message based`() {
        val item = GroupedMessageItem.Single(message(id = 99L))

        assertEquals("msg_99", item.lazyItemKey)
    }

    private fun message(id: Long, albumId: Long = 0L): MessageModel =
        MessageModel(
            id = id,
            date = 0,
            isOutgoing = false,
            senderName = "sender",
            chatId = 1L,
            content = MessageContent.Text("text"),
            mediaAlbumId = albumId
        )
}
