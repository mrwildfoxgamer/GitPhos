package com.example.gitphos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_queue")
data class UploadQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val repoId: Long,
    val filePath: String,
    val status: String,       // PENDING | IN_PROGRESS | DONE | FAILED
    val retryCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)