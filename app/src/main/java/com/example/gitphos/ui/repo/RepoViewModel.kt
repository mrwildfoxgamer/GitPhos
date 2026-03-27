package com.example.gitphos.ui.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitphos.data.local.datastore.PrefsDataStore
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoViewModel @Inject constructor(
    private val repoMetadataDao: RepoMetadataDao,
    private val prefsDataStore: PrefsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(RepoState())
    val state: StateFlow<RepoState> = _state.asStateFlow()

    private val _effect = Channel<RepoEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        repoMetadataDao.observeAll()
            .onEach { repos -> _state.update { it.copy(repos = repos, isLoading = false) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            prefsDataStore.userPrefs
                .onEach { prefs -> _state.update { it.copy(activeRepoId = prefs.activeRepoId) } }
                .launchIn(this)
        }
    }

    fun onEvent(event: RepoEvent) {
        when (event) {
            RepoEvent.ShowAddDialog -> _state.update { it.copy(showAddDialog = true, dialogError = null) }
            RepoEvent.DismissDialog -> resetDialog()
            is RepoEvent.DialogNameChanged -> _state.update { it.copy(dialogName = event.value) }
            is RepoEvent.DialogRemoteUrlChanged -> _state.update { it.copy(dialogRemoteUrl = event.value) }
            is RepoEvent.DialogLocalPathChanged -> _state.update { it.copy(dialogLocalPath = event.value) }
            is RepoEvent.DialogBranchChanged -> _state.update { it.copy(dialogBranch = event.value) }
            RepoEvent.ConfirmAdd -> saveRepo()
            is RepoEvent.SetActive -> setActive(event.repo)
            is RepoEvent.DeleteRepo -> deleteRepo(event.repo)
            RepoEvent.NavigateBack -> viewModelScope.launch { _effect.send(RepoEffect.NavigateBack) }
        }
    }

    private fun saveRepo() {
        val s = _state.value
        val error = when {
            s.dialogName.isBlank() -> "Name is required"
            s.dialogRemoteUrl.isBlank() -> "Remote URL is required"
            !s.dialogRemoteUrl.startsWith("https://github.com/") -> "URL must start with https://github.com/"
            s.dialogLocalPath.isBlank() -> "Local path is required"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(dialogError = error) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val id = repoMetadataDao.insert(
                RepoMetadataEntity(
                    name = s.dialogName.trim(),
                    remoteUrl = s.dialogRemoteUrl.trim(),
                    localPath = s.dialogLocalPath.trim(),
                    branch = s.dialogBranch.trim().ifBlank { "main" }
                )
            )
            resetDialog()
            _state.update { it.copy(isSaving = false) }
            _effect.send(RepoEffect.ShowMessage("Repository added"))
            // Auto-set as active if it's the first one
            if (_state.value.repos.size == 1) {
                setActiveById(id, s.dialogLocalPath.trim())
            }
        }
    }

    private fun setActive(repo: RepoMetadataEntity) {
        viewModelScope.launch {
            setActiveById(repo.id, repo.localPath)
            _effect.send(RepoEffect.ShowMessage("\"${repo.name}\" set as active"))
        }
    }

    private suspend fun setActiveById(id: Long, localPath: String) {
        repoMetadataDao.clearActiveRepo()
        repoMetadataDao.setActiveRepo(id)
        prefsDataStore.setActiveRepo(id, localPath)
        _state.update { it.copy(activeRepoId = id) }
    }

    private fun deleteRepo(repo: RepoMetadataEntity) {
        viewModelScope.launch {
            repoMetadataDao.delete(repo)
            if (_state.value.activeRepoId == repo.id) {
                prefsDataStore.setActiveRepo(-1L, "")
                _state.update { it.copy(activeRepoId = -1L) }
            }
            _effect.send(RepoEffect.ShowMessage("\"${repo.name}\" deleted"))
        }
    }

    private fun resetDialog() {
        _state.update {
            it.copy(
                showAddDialog = false,
                dialogName = "",
                dialogRemoteUrl = "",
                dialogLocalPath = "",
                dialogBranch = "main",
                dialogError = null
            )
        }
    }
}