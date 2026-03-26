package com.example.gitphos.ui.auth
data class AuthState(
    val token: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthEvent {
    data class TokenChanged(val value: String) : AuthEvent
    data object Submit : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToDashboard : AuthEffect
}