package com.example.socraticai.ai

import android.content.Context

class PromptRenderer(private val context: Context) {

    /**
     * Renders a Socratic prompt by injecting context and history into the template.
     * Mimics Open-Notebook's $CONTEXT injection logic.
     */
    fun render(
        templateName: String, // e.g., "socratic_tutor.md"
        contextData: String = "",
        userQuery: String,
        history: String = ""
    ): String {
        val agentsRules = readAgentsMd()
        val template = readTemplate(templateName)
        
        return template
            .replace("\$RULES", agentsRules)
            .replace("\$CONTEXT", contextData)
            .replace("\$HISTORY", history)
            .replace("\$USER_QUERY", userQuery)
    }

    private fun readAgentsMd(): String {
        return try {
            context.assets.open("AGENTS.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Follow Socratic Tutoring principles: guide the student, don't give answers."
        }
    }

    private fun readTemplate(name: String): String {
        return try {
            context.assets.open("prompts/$name").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            """
            ${'$'}RULES
            
            Context: ${'$'}CONTEXT
            
            History: ${'$'}HISTORY
            
            Student Question: ${'$'}USER_QUERY
            
            Your Guiding Response:
            """.trimIndent()
        }
    }
}
