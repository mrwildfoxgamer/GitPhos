package com.example.gitphos.domain.usecase

import com.example.gitphos.domain.model.GithubUser
import com.example.gitphos.domain.repository.AuthRepository
import javax.inject.Inject

class ValidateTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(pat: String): Result<GithubUser> {
        if (pat.isBlank()) return Result.failure(IllegalArgumentException("Token is empty"))
        return repository.validateAndSaveToken(pat)
    }
}