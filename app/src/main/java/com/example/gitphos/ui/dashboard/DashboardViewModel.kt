package com.example.gitphos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.dao.SyncHistoryDao
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.domain.usecase.LogoutUseCase
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
class DashboardViewModel @Inject constructor(
    private val repoMetadataDao: RepoMetadataDao,
    private val syncHistoryDao: SyncHistoryDao,
    private val uploadQueueDao: UploadQueueDao,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effect = Channel<DashboardEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadDashboard()
    }

     fun loadDashboard() {
        viewModelScope.launch {
            val activeRepo = repoMetadataDao.getActiveRepo()
            val pendingCount = uploadQueueDao.getPendingItems().size
            val lastSync = activeRepo?.let { syncHistoryDao.getLastSync(it.id) }

            _state.update {
                it.copy(
                    activeRepo = activeRepo,
                    pendingCount = pendingCount,
                    lastSync = lastSync,
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.SyncNow -> viewModelScope.launch {
                _effect.send(DashboardEffect.NavigateToSync)
            }
            DashboardEvent.PickImages -> viewModelScope.launch {
                if (_state.value.activeRepo == null) {
                    _effect.send(DashboardEffect.ShowError("Set up a repository first"))
                } else {
                    _effect.send(DashboardEffect.NavigateToPicker)
                }
            }
            DashboardEvent.ManageRepo -> viewModelScope.launch {
                _effect.send(DashboardEffect.NavigateToRepo)
            }
            DashboardEvent.Logout -> viewModelScope.launch {
                logoutUseCase()
                _effect.send(DashboardEffect.NavigateToAuth)
            }
        }
    }
}