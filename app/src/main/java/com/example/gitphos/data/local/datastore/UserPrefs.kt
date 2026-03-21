package com.example.gitphos.data.local.datastore

data class UserPrefs(
    val authToken: String = "",
    val githubUsername: String = "",
    val activeRepoPath: String = "",
    val activeRepoId: Long = -1L,
    val autoSyncEnabled: Boolean = false,
    val syncIntervalMins: Long = 30L,
    val theme: String = "SYSTEM"
)