package com.example.gitphos.domain.repository

import com.example.gitphos.domain.model.GitResult

interface GitRepository {
    suspend fun initRepository(localPath: String): GitResult<Unit>
    suspend fun cloneRepository(remoteUrl: String, localPath: String, token: String): GitResult<Unit>
    suspend fun addFiles(localPath: String, filePatterns: List<String> = listOf(".")): GitResult<Unit>
    suspend fun commit(localPath: String, message: String, authorName: String, authorEmail: String): GitResult<Unit>
    suspend fun push(localPath: String, remoteUrl: String, token: String, branch: String = "main", onProgress: ((String) -> Unit)? = null): GitResult<Unit>
    suspend fun getRepoSize(localPath: String): GitResult<Long>
    suspend fun ensureBranch(localPath: String, branch: String = "main"): GitResult<Unit>
    suspend fun isValidRepo(localPath: String): Boolean
}