package com.example.gitphos.domain.model

data class GithubUser(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val email: String?
)