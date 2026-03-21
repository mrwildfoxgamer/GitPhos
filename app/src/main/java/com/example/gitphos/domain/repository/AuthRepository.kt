package com.example.gitphos.domain.repository

import com.example.gitphos.domain.model.GithubUser

interface AuthRepository {
    suspend fun validateAndSaveToken(pat: String): Result<GithubUser>
    suspend fun logout()
    suspend fun getStoredToken(): String? // <-- Add this line
}