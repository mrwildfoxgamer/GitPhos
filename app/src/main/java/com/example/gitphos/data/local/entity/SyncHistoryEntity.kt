package com.example.gitphos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_history")
data class SyncHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val repoId: Long,
    val commitHash: String?,
    val filesChanged: Int,
    val status: String,       // SUCCESS | FAILED
    val triggeredBy: String,  // MANUAL | AUTO
    val syncedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)