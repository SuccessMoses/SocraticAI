package com.example.socraticai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.socraticai.ai.GemmaLiteRTClient
import com.example.socraticai.ai.ModelRouter
import com.example.socraticai.ai.SocraticAgent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var socraticAgent: SocraticAgent? = null

    init {
        initializeAgent()
    }

    private fun initializeAgent() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Initializing AI Models...")
            
            // Placeholder path - in a real app, we'd download this or pick it.
            val modelPath = File(getApplication<Application>().filesDir, "gemma.litertlm").absolutePath
            
            val client = GemmaLiteRTClient(getApplication(), modelPath)
            client.initialize()
            
            val router = ModelRouter(fastClient = null, deepClient = client)
            socraticAgent = SocraticAgent(router)
            
            _uiState.value = UiState.Ready
        }
    }

    fun askQuestion(question: String) {
        val agent = socraticAgent ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Thinking
            var responseText = ""
            agent.ask(question)?.collect { partial ->
                responseText += partial
                _uiState.value = UiState.Responding(responseText)
            } ?: run {
                _uiState.value = UiState.Error("Model not initialized or failed to respond.")
            }
        }
    }
}

sealed class UiState {
    object Idle : UiState()
    data class Loading(val message: String) : UiState()
    object Ready : UiState()
    object Thinking : UiState()
    data class Responding(val text: String) : UiState()
    data class Error(val message: String) : UiState()
}
