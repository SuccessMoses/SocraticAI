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
    fun route(prompt: String): ModelTier {
        // Simple heuristic for now: if prompt is short, use FAST.
        // In a real agentic framework, we'd use a tiny classifier model here.
        return if (prompt.length < 50) ModelTier.FAST else ModelTier.DEEP
    }

    fun getResponse(prompt: String): Flow<String>? {
        val tier = route(prompt)
        return when (tier) {
            ModelTier.FAST -> fastClient?.sendMessageStream(prompt) ?: deepClient?.sendMessageStream(prompt)
            ModelTier.DEEP -> deepClient?.sendMessageStream(prompt) ?: fastClient?.sendMessageStream(prompt)
        }
    }
}
