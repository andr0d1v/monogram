package org.monogram.presentation.features.chats.currentChat.components.inputbar

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import org.monogram.domain.models.StickerModel

internal fun insertEmojiAtSelection(
    value: TextFieldValue,
    emoji: String,
    sticker: StickerModel?,
    knownCustomEmojis: MutableMap<Long, StickerModel>
): TextFieldValue {
    val currentText = value.annotatedString
    val selection = value.selection

    val emojiAnnotated = if (sticker != null) {
        val customEmojiEntityId = sticker.customEmojiId ?: sticker.id
        knownCustomEmojis[customEmojiEntityId] = sticker
        val symbol = emoji.ifBlank { sticker.emoji.ifBlank { "\uD83D\uDE42" } }
        buildAnnotatedString {
            append(symbol)
            addStringAnnotation(CUSTOM_EMOJI_TAG, customEmojiEntityId.toString(), 0, symbol.length)
        }
    } else {
        AnnotatedString(emoji.ifBlank { "\uD83D\uDE42" })
    }

    val newText = buildAnnotatedString {
        append(currentText.subSequence(0, selection.start))
        append(emojiAnnotated)
        append(currentText.subSequence(selection.end, currentText.length))
    }

    return value.copy(
        annotatedString = newText,
        selection = TextRange(selection.start + emojiAnnotated.length)
    )
}
