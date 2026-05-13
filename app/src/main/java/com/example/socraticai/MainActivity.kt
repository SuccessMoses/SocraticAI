package com.example.socraticai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socraticai.ui.MainViewModel
import com.example.socraticai.ui.UiState
import com.example.socraticai.ui.theme.SocraticAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocraticAITheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SocraticChatScreen(
                        uiState = uiState,
                        onAsk = { viewModel.askQuestion(it) },
                        onPickDocument = { viewModel.ingestDocument(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SocraticChatScreen(
    uiState: UiState,
    onAsk: (String) -> Unit,
    onPickDocument: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onPickDocument(it) }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
                Text(text = uiState.message)
            }
            is UiState.Error -> {
                Text(text = "Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                if (uiState is UiState.Thinking) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Text(text = uiState.step, style = MaterialTheme.typography.bodySmall)
                    }
                }

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Ask a question...") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                
                if (uiState is UiState.Responding) {
                    Text(
                        text = uiState.text, 
                        modifier = Modifier.weight(1f).padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { launcher.launch("*/*") }) {
                        Icon(Icons.Default.Add, contentDescription = "Upload Notes")
                    }
                    
                    Spacer(modifier = Modifier.size(8.dp))

                    Button(
                        onClick = { onAsk(text) },
                        enabled = uiState is UiState.Ready || uiState is UiState.Responding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ask SocraticAI")
                    }
                }
            }
        }
    }
}
