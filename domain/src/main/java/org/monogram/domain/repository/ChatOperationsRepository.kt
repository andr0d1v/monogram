package org.monogram.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ChatOperationsRepository {
    val isArchivePinned: StateFlow<Boolean>
    val isArchiveAlwaysVisible: StateFlow<Boolean>

    fun toggleMuteChats(chatIds: Set<Long>, mute: Boolean)
    fun toggleArchiveChats(chatIds: Set<Long>, archive: Boolean)
    fun togglePinChats(chatIds: Set<Long>, pin: Boolean, folderId: Int)
    fun toggleReadChats(chatIds: Set<Long>, markAsUnread: Boolean)
    fun markChatsAsRead(chatIds: Set<Long>)
    fun markFolderAsRead(folderId: Int, chatIds: Set<Long>)
    fun deleteChats(chatIds: Set<Long>)
    fun leaveChats(chatIds: Set<Long>)
    fun leaveChat(chatId: Long)
    fun setArchivePinned(pinned: Boolean)

    fun clearChatHistories(chatIds: Set<Long>, revoke: Boolean)
    fun clearChatHistory(chatId: Long, revoke: Boolean)
    suspend fun getChatLink(chatId: Long): String?
    fun reportChats(chatIds: Set<Long>, reason: String, messageIds: List<Long> = emptyList())
    fun reportChat(chatId: Long, reason: String, messageIds: List<Long> = emptyList())
}