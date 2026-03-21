package com.example.gitphos.data.repository

import com.example.gitphos.data.local.datastore.PrefsDataStore
import com.example.gitphos.data.remote.GithubApi
import com.example.gitphos.domain.model.GithubUser
import com.example.gitphos.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: GithubApi,
    private val prefs: PrefsDataStore
) : AuthRepository {

    override suspend fun validateAndSaveToken(pat: String): Result<GithubUser> {
        return try {
            val dto = api.getAuthenticatedUser("token $pat")
            val user = GithubUser(
                id        = dto.id,
                login     = dto.login,
                avatarUrl = dto.avatarUrl,
                name      = dto.name,
                email     = dto.email
            )
            prefs.setAuthToken(pat)
            prefs.setGithubUsername(user.login)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        prefs.clearAuth()
    }
}