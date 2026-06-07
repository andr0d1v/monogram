package org.monogram.presentation.features.chats.conversation.logic

import android.util.Log
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.monogram.domain.models.MessageContent
import org.monogram.presentation.features.chats.conversation.DefaultChatComponent

internal fun DefaultChatComponent.handleDownloadFile(fileId: Int) {
    repositoryMessage.downloadFile(fileId, priority = 32)
}

internal fun DefaultChatComponent.handleCancelDownloadFile(fileId: Int) {
    scope.launch {
        try {
            repositoryMessage.cancelDownloadFile(fileId)
        } catch (e: Throwable) {
            Log.e("DownloadDebug", "CancelDownloadFile failed: fileId=$fileId chatId=$chatId", e)
        }
    }
}

internal fun DefaultChatComponent.handleDownloadHighRes(messageId: Long) {
    scope.launch {
        val fileId = repositoryMessage.getHighResFileId(chatId, messageId)
        if (fileId != null) {
            updatePhotoOriginalFileId(messageId, fileId)
            repositoryMessage.downloadFile(fileId, priority = 32)
        }
    }
}

private fun DefaultChatComponent.updatePhotoOriginalFileId(messageId: Long, originalFileId: Int) {
    if (originalFileId == 0) return
    _state.update { state ->
        state.copy(
            messages = state.messages.map { message ->
                if (message.id != messageId) return@map message
                val photo = message.content as? MessageContent.Photo ?: return@map message
                if (photo.originalFileId == originalFileId) {
                    message
                } else {
                    message.copy(content = photo.copy(originalFileId = originalFileId))
                }
            }
        )
    }
}
