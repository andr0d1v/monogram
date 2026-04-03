package org.monogram.presentation.features.chats.currentChat.components.inputbar

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

data class EditorSnippet(
    val title: String,
    val text: String
)

class EditorSnippetStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<EditorSnippet> {
        val raw = prefs.getString(KEY_SNIPPETS, null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    val title = item.optString("title")
                    val text = item.optString("text")
                    if (title.isNotBlank() && text.isNotBlank()) {
                        add(EditorSnippet(title = title, text = text))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(snippets: List<EditorSnippet>) {
        val json = JSONArray()
        snippets.forEach { snippet ->
            json.put(
                JSONObject().apply {
                    put("title", snippet.title)
                    put("text", snippet.text)
                }
            )
        }
        prefs.edit().putString(KEY_SNIPPETS, json.toString()).apply()
    }

    private companion object {
        private const val PREFS_NAME = "fullscreen_editor_features"
        private const val KEY_SNIPPETS = "snippets"
    }
}

fun findOccurrences(text: String, query: String, ignoreCase: Boolean = true): List<IntRange> {
    if (query.isBlank()) return emptyList()
    val result = mutableListOf<IntRange>()
    var start = 0
    while (start < text.length) {
        val index = text.indexOf(query, startIndex = start, ignoreCase = ignoreCase)
        if (index == -1) break
        val endExclusive = index + query.length
        result += index until endExclusive
        start = max(index + 1, endExclusive)
    }
    return result
}

fun applyReplaceAtRange(
    currentValue: TextFieldValue,
    range: IntRange,
    replacement: String
): TextFieldValue {
    val oldText = currentValue.text
    if (range.first !in oldText.indices) return currentValue
    val endExclusive = range.last + 1
    if (endExclusive > oldText.length || range.first >= endExclusive) return currentValue

    val newText = oldText.replaceRange(range.first, endExclusive, replacement)
    val incoming = TextFieldValue(
        text = newText,
        selection = TextRange(range.first + replacement.length)
    )
    return mergeInputTextValuePreservingAnnotations(currentValue, incoming)
}

fun applyReplaceAll(
    currentValue: TextFieldValue,
    query: String,
    replacement: String
): TextFieldValue {
    if (query.isBlank()) return currentValue
    val newText = currentValue.text.replace(query, replacement, ignoreCase = true)
    val incoming = TextFieldValue(text = newText, selection = TextRange(newText.length))
    return mergeInputTextValuePreservingAnnotations(currentValue, incoming)
}
