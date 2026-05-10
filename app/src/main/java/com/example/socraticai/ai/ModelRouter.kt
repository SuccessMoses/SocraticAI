package com.example.socraticai.ai

import kotlinx.coroutines.flow.Flow

enum class ModelTier {
    FAST, // e.g. 270M
    DEEP  // e.g. Gemma 4B
}

class ModelRouter(
    private val fastClient: GemmaLiteRTClient?,
    private val deepClient: GemmaLiteRTClient?
) {
    /**
     * Intent Classification logic grounded in Open-Notebook's Esperanto layer.
     * Uses the FAST model (270M) to quickly decide the query's nature.
     */
    suspend fun classifyIntent(prompt: String): ModelTier {
        // In the full implementation, we call fastClient with a classification prompt.
        // For Day 7 prototype, we simulate the <100ms response time.
        return if (prompt.contains("clear", ignoreCase = true) || 
            prompt.contains("help", ignoreCase = true) ||
            prompt.length < 30) {
            ModelTier.FAST
        } else {
            ModelTier.DEEP
        }
    }

    fun getResponse(prompt: String, tier: ModelTier): Flow<String>? {
        return when (tier) {
            ModelTier.FAST -> fastClient?.sendMessageStream(prompt) ?: deepClient?.sendMessageStream(prompt)
            ModelTier.DEEP -> deepClient?.sendMessageStream(prompt) ?: fastClient?.sendMessageStream(prompt)
        }
    }
}
