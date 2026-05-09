package com.example.socraticai.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File

class GemmaLiteRTClient(
    private val context: Context,
    private val modelPath: String
) {
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var isMockMode = false

    suspend fun initialize() {
        if (File(modelPath).exists()) {
            val config = EngineConfig(
                modelPath = modelPath,
                backend = Backend.GPU(),
                maxNumTokens = 4096
            )
            engine = Engine(config).apply {
                initialize()
            }
            conversation = engine?.createConversation()
            isMockMode = false
        } else {
            isMockMode = true
        }
    }

    fun sendMessageStream(prompt: String): Flow<String>? {
        if (isMockMode) {
            return flow {
                val mockResponse = "I'm in mock mode because the Gemma weights weren't found at $modelPath. Once you add the weights, I'll be much smarter!"
                mockResponse.split(" ").forEach { word ->
                    emit("$word ")
                    delay(100)
                }
            }
        }
        return conversation?.sendMessageAsync(prompt)?.map { message ->
            message.toString()
        }
    }

    fun close() {
        conversation = null
        engine?.close()
        engine = null
    }
}
