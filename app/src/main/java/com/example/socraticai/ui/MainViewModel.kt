package com.example.socraticai.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.socraticai.ai.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var socraticOrchestrator: SocraticOrchestrator? = null
    private var sourceIngestor: SourceIngestor? = null

    init {
        initializeAgent()
    }

    private fun initializeAgent() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Initializing Socratic AI Models...")
            
            val filesDir = getApplication<Application>().filesDir
            val gemma4Path = File(filesDir, "gemma_4b.litertlm").absolutePath
            val gemmaTinyPath = File(filesDir, "gemma_270m.litertlm").absolutePath
            
            val deepClient = GemmaLiteRTClient(getApplication(), gemma4Path)
            deepClient.initialize()
            
            val fastClient = GemmaLiteRTClient(getApplication(), gemmaTinyPath)
            fastClient.initialize()
            
            val visionClient = GemmaVisionClient(getApplication(), gemma4Path)
            visionClient.initialize()
            
            val router = ModelRouter(fastClient = fastClient, deepClient = deepClient)
            val renderer = PromptRenderer(getApplication())
            
            socraticOrchestrator = SocraticOrchestrator(router, renderer)
            
            sourceIngestor = SourceIngestor(
                getApplication(), 
                visionClient, 
                VectorSearchManager()
            )
            
            _uiState.value = UiState.Ready
        }
    }

    fun ingestDocument(uri: Uri) {
        val ingestor = sourceIngestor ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Thinking("Analyzing your materials...")
            ingestor.ingest(uri)
            _uiState.value = UiState.Ready
        }
    }

    fun askQuestion(question: String) {
        val orchestrator = socraticOrchestrator ?: return
        viewModelScope.launch {
            launch {
                orchestrator.currentState.collect { socraticState ->
                    updateUiWithSocraticState(socraticState)
                }
            }

            var responseText = ""
            orchestrator.processUserQuery(question)?.collect { partial ->
                responseText += partial
                _uiState.value = UiState.Responding(responseText)
            } ?: run {
                _uiState.value = UiState.Error("Tutor failed to process query.")
            }
        }
    }

    private fun updateUiWithSocraticState(state: SocraticState) {
        when (state) {
            is SocraticState.ContextCheck -> _uiState.value = UiState.Thinking("Checking study materials...")
            is SocraticState.MapPhase -> _uiState.value = UiState.Thinking("Analyzing chunks of your notes...")
            is SocraticState.ReducePhase -> _uiState.value = UiState.Thinking("Synthesizing a guiding question...")
            is SocraticState.GenerateGuide -> _uiState.value = UiState.Thinking("Crafting response...")
            else -> { /* Responding state handled by flow collection */ }
        }
    }
}

sealed class UiState {
    object Idle : UiState()
    data class Loading(val message: String) : UiState()
    object Ready : UiState()
    data class Thinking(val step: String) : UiState()
    data class Responding(val text: String) : UiState()
    data class Error(val message: String) : UiState()
}
