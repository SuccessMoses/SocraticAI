package com.example.socraticai.ai

import com.example.socraticai.data.Note
import com.example.socraticai.data.Note_
import com.example.socraticai.data.ObjectBox
import com.example.socraticai.data.Source
import io.objectbox.Box

class VectorSearchManager {
    private val noteBox: Box<Note> = ObjectBox.boxStore.boxFor(Note::class.java)
    private val sourceBox: Box<Source> = ObjectBox.boxStore.boxFor(Source::class.java)

    fun addSource(name: String, content: String, type: String): Long {
        val source = Source(name = name, content = content, type = type)
        return sourceBox.put(source)
    }

    fun addNoteWithEmbedding(content: String, embedding: FloatArray, sourceId: Long): Long {
        val source = sourceBox.get(sourceId)
        val note = Note(content = content, type = "SOURCE_CHUNK", embedding = embedding)
        note.sourceArtifact.target = source
        return noteBox.put(note)
    }

    fun searchSimilar(queryEmbedding: FloatArray, limit: Int = 5): List<Note> {
        val query = noteBox.query()
            .nearestNeighbors(Note_.embedding, queryEmbedding, limit)
            .build()
        
        return query.find()
    }
}
