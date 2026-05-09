package com.example.socraticai.ai

import kotlinx.coroutines.flow.Flow

class SocraticAgent(
    private val modelRouter: ModelRouter
) {
    fun ask(userPrompt: String): Flow<String>? {
        // In the future, this is where we would inject RAG context
        // or format the prompt for the Socratic method if the model needs it.
        return modelRouter.getResponse(userPrompt)
    }
}
