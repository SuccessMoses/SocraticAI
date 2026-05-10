package com.example.socraticai.ai

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

class LongContextManager {

    /**
     * Constants grounded in open-notebook/utils/chunking.py
     * Default chunk size is 400 tokens (~1600 chars).
     * Default overlap is 15% (60 tokens / 240 chars).
     */
    private val chunkTokens = 400
    private val charsPerToken = 4
    private val maxChars = chunkTokens * charsPerToken
    private val overlapChars = (maxChars * 0.15).toInt()

    /**
     * Implements the "Optimistic Map-Reduce" algorithm.
     * Grounded in open_notebook/graphs/transformation.py
     */
    suspend fun processWithMapReduce(
        fullContext: String,
        userQuery: String,
        processor: suspend (context: String, query: String) -> String,
        synthesizer: suspend (results: List<String>) -> String
    ): String {
        return try {
            // Phase 1: Optimistic Attempt
            processor(fullContext, userQuery)
        } catch (e: Exception) {
            // Check if it's a context window error (simplified for mock/prototype)
            if (isContextError(e)) {
                val chunks = chunkContent(fullContext)
                
                // Phase 2: Map Phase (Parallel processing)
                val results = coroutineScope {
                    chunks.map { chunk ->
                        async { processor(chunk, userQuery) }
                    }.awaitAll()
                }
                
                // Phase 3: Reduce Phase (Synthesis)
                synthesizer(results)
            } else {
                throw e
            }
        }
    }

    private fun chunkContent(content: String): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < content.length) {
            val end = (start + maxChars).coerceAtMost(content.length)
            chunks.add(content.substring(start, end))
            if (end == content.length) break
            start += maxChars - overlapChars
        }
        return chunks
    }

    private fun isContextError(e: Exception): Boolean {
        // In LiteRT-LM, we would check for a specific exception type or message
        // For the prototype/mock, we'll assume long text > 8000 chars triggers it
        return e.message?.contains("context", ignoreCase = true) == true || 
               e is IllegalArgumentException // Common fallback for SDK errors
    }
}
