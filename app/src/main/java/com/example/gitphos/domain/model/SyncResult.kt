package com.example.gitphos.domain.model

sealed class SyncResult {
    data object Success : SyncResult()
    data class PartialSuccess(val uploaded: Int, val failed: Int) : SyncResult()
    data class Failure(val reason: String, val cause: Throwable? = null) : SyncResult()
}