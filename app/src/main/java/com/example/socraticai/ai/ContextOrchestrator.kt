package com.example.socraticai.ai

import com.example.socraticai.data.Note
import com.example.socraticai.data.Source
import com.example.socraticai.data.ObjectBox
import io.objectbox.Box

data class ContextItem(
    val id: String,
    val type: String, // "source", "note", "insight"
    val content: String,
    val priority: Int,
    val tokenCount: Int
)

class ContextOrchestrator {
    private val noteBox: Box<Note> = ObjectBox.boxStore.boxFor(Note::class.java)
    private val sourceBox: Box<Source> = ObjectBox.boxStore.boxFor(Source::class.java)

    /**
     * Implementation grounded in open-notebook/utils/context_builder.py
     * Priorities: source=100, insight=75, note=50
     */
    fun buildContext(maxTokens: Int): String {
        val items = mutableListOf<ContextItem>()

        // 1. Gather all Sources
        sourceBox.all.forEach { source ->
            items.add(ContextItem(
                id = "source:${source.id}",
                type = "source",
                content = source.content ?: "",
                priority = 100,
                tokenCount = estimateTokens(source.content ?: "")
            ))
        }

        // 2. Gather all Notes (including AI guides)
        noteBox.all.forEach { note ->
            val priority = if (note.type == "AI_GUIDE") 75 else 50
            items.add(ContextItem(
                id = "note:${note.id}",
                type = if (note.type == "AI_GUIDE") "insight" else "note",
                content = note.content ?: "",
                priority = priority,
                tokenCount = estimateTokens(note.content ?: "")
            ))
        }

        // 3. Prioritize (Higher priority first)
        items.sortByDescending { it.priority }

        // 4. Truncate to fit
        val selectedItems = mutableListOf<ContextItem>()
        var currentTokens = 0
        for (item in items) {
            if (currentTokens + item.tokenCount <= maxTokens) {
                selectedItems.add(item)
                currentTokens += item.tokenCount
            } else {
                // Grounded in open-notebook pattern: stop when full
                break
            }
        }

        // 5. Format response (Render as a grounded context string)
        return selectedItems.joinToString("\n\n") { item ->
            "--- ${item.type.uppercase()}: ${item.id} ---\n${item.content}"
        }
    }

    private fun estimateTokens(text: String): Int {
        // Fallback grounded in open-notebook/utils/token_utils.py: word count * 1.3
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return (words * 1.3).toInt()
    }
}
