package com.example.gitphos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repo_metadata")
data class RepoMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val localPath: String,
    val remoteUrl: String,
    val branch: String = "main",
    val isActive: Boolean = false,
    val lastSyncAt: Long? = null,
    val totalCommits: Int = 0,
    val diskUsageBytes: Long = 0
)