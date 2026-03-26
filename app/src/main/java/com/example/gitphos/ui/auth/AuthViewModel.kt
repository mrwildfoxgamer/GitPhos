package com.example.gitphos.ui.auth

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
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.TokenChanged -> _state.update { it.copy(token = event.value, error = null) }
            is AuthEvent.Submit -> authenticate()
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
                    _state.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Invalid token")
                    }
                }
        }
    }
}