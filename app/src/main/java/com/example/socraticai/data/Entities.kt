package com.example.socraticai.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
data class Source(
    @Id var id: Long = 0,
    var name: String? = null,
    var content: String? = null,
    var type: String? = null // e.g., "PDF", "Text", "URL"
)

@Entity
data class Note(
    @Id var id: Long = 0,
    var content: String? = null,
    var type: String? = null, // e.g., "AI_GUIDE", "USER_NOTE"
    
    @HnswIndex(dimensions = 384)
    var embedding: FloatArray? = null
) {
    lateinit var sourceArtifact: ToOne<Source>
}
