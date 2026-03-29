package com.example.gitphos.ui.auth

import com.example.gitphos.data.remote.GithubApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitphos.domain.usecase.ValidateTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val githubApi: GithubApi
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.TokenChanged -> _state.update { it.copy(token = event.value, error = null) }
            is AuthEvent.Submit -> authenticate()
            is AuthEvent.HandleOAuthCode -> handleOAuthCode(event.code)
        }
    }

    private fun handleOAuthCode(code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = githubApi.getAccessToken(
                    clientId = "Ov23liDrHHPSD36PjrgJ",
                    clientSecret = "3d57c6dd13b0e70936412b8db40b0428c0193b18",
                    code = code
                )
                validateTokenUseCase(response.accessToken)
                    .onSuccess { _effect.send(AuthEffect.NavigateToDashboard) }
                    .onFailure { throwable ->
                        _state.update { it.copy(isLoading = false, error = throwable.message ?: "Invalid token") }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "OAuth failed") }
            }
        }
    }

    private fun authenticate() {
        val token = _state.value.token.trim()
        if (token.isBlank()) {
            _state.update { it.copy(error = "Token cannot be empty") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            validateTokenUseCase(token)
                .onSuccess { _effect.send(AuthEffect.NavigateToDashboard) }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Invalid token") }
                }
        }
    }
}