package com.example.socraticai.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.socraticai.data.Note
import com.example.socraticai.data.ObjectBox
import com.example.socraticai.data.Source
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Manages document and image ingestion.
 * Grounded in Open-Notebook's `graphs/source.py` and `content-core`.
 */
class SourceIngestor(
    private val context: Context,
    private val visionClient: GemmaVisionClient,
    private val vectorSearchManager: VectorSearchManager
) {
    private val sourceBox: Box<Source> = ObjectBox.boxStore.boxFor(Source::class.java)

    suspend fun ingest(uri: Uri) = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri) ?: "Unknown Source"
        val mimeType = context.contentResolver.getType(uri) ?: ""

        val rawContent = when {
            mimeType.startsWith("image/") -> {
                // Grounded in Open-Notebook's Vision Extraction
                // In a real implementation, we'd convert Uri to Bitmap and call visionClient
                "Extracted text from handwritten image: $fileName"
            }
            mimeType == "application/pdf" -> {
                // Grounded in Open-Notebook's PDF Extraction
                "Extracted text from PDF document: $fileName"
            }
            else -> {
                // Plain text fallback
                context.contentResolver.openInputStream(uri)?.use { 
                    it.bufferedReader().readText() 
                } ?: ""
            }
        }

        // Save Source
        val sourceId = vectorSearchManager.addSource(fileName, rawContent, mimeType)
        
        // Chunk and Embed (Grounded in open-notebook/utils/chunking.py)
        // For prototype, we simulate the embedding phase
        vectorSearchManager.addNoteWithEmbedding(
            content = rawContent, 
            embedding = FloatArray(384) { 0.1f }, 
            sourceId = sourceId
        )
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = cursor.getString(index)
            }
        }
        return name
    }
}
