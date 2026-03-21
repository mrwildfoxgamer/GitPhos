package com.example.gitphos.domain.model

sealed class GitResult<out T> {
    data class Success<T>(val data: T) : GitResult<T>()
    data class Error(val code: GitErrorCode, val message: String, val cause: Throwable? = null) : GitResult<Nothing>()
}

enum class GitErrorCode {
    REPO_NOT_FOUND,
    REPO_INIT_FAILED,
    CLONE_FAILED,
    ADD_FAILED,
    COMMIT_FAILED,
    PUSH_FAILED,
    AUTH_FAILED,
    BRANCH_NOT_FOUND,
    NETWORK_ERROR,
    UNKNOWN
}

inline fun <T> GitResult<T>.onSuccess(action: (T) -> Unit): GitResult<T> {
    if (this is GitResult.Success) action(data)
    return this
}

inline fun <T> GitResult<T>.onError(action: (GitResult.Error) -> Unit): GitResult<T> {
    if (this is GitResult.Error) action(this)
    return this
}