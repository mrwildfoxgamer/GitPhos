package com.example.gitphos.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.gitphos.data.local.datastore.PrefsDataStore
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.worker.SyncScheduler
import com.example.gitphos.worker.SyncWorker
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
class SyncViewModel @Inject constructor(
    private val uploadQueueDao: UploadQueueDao,
    private val repoMetadataDao: RepoMetadataDao,
    private val prefsDataStore: PrefsDataStore,
    private val syncScheduler: SyncScheduler,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _effect = Channel<SyncEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val activeRepo = repoMetadataDao.getActiveRepo()
            if (activeRepo == null) {
                _state.update { it.copy(noActiveRepo = true) }
                return@launch
            }
            _state.update {
                it.copy(
                    activeRepoName = activeRepo.name,
                    remoteUrl = activeRepo.remoteUrl
                )
            }
            observeQueue(activeRepo.id)
            observeWorker()
        }
    }

    private fun observeQueue(repoId: Long) {
        uploadQueueDao.observeByRepo(repoId)
            .onEach { items -> _state.update { it.copy(pendingItems = items) } }
            .launchIn(viewModelScope)
    }

    private fun observeWorker() {
        workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
            .observeForever { infos ->
                val info = infos?.firstOrNull() ?: return@observeForever
                val status = when (info.state) {
                    WorkInfo.State.RUNNING -> WorkStatus.Running
                    WorkInfo.State.SUCCEEDED -> {
                        val uploaded = info.outputData.getInt(SyncWorker.KEY_UPLOADED_COUNT, 0)
                        val failed = info.outputData.getInt(SyncWorker.KEY_FAILED_COUNT, 0)
                        if (failed > 0) WorkStatus.PartialSuccess(uploaded, failed)
                        else WorkStatus.Success(uploaded)
                    }
                    WorkInfo.State.FAILED -> {
                        val reason = info.outputData.getString(SyncWorker.KEY_ERROR_MESSAGE) ?: "Unknown error"
                        WorkStatus.Failed(reason)
                    }
                    WorkInfo.State.CANCELLED -> WorkStatus.Idle
                    else -> WorkStatus.Idle
                }
                _state.update { it.copy(isSyncing = info.state == WorkInfo.State.RUNNING, workStatus = status) }
            }
    }

    fun onEvent(event: SyncEvent) {
        when (event) {
            SyncEvent.StartSync -> {
                val remoteUrl = _state.value.remoteUrl
                if (remoteUrl.isBlank()) {
                    viewModelScope.launch { _effect.send(SyncEffect.ShowMessage("No remote URL configured")) }
                    return
                }
                syncScheduler.scheduleSync(remoteUrl)
                _state.update { it.copy(isSyncing = true, workStatus = WorkStatus.Running) }
            }
            SyncEvent.CancelSync -> {
                syncScheduler.cancelSync()
                _state.update { it.copy(isSyncing = false, workStatus = WorkStatus.Idle) }
            }
            SyncEvent.ClearCompleted -> viewModelScope.launch {
                uploadQueueDao.clearCompleted()
                _effect.send(SyncEffect.ShowMessage("Completed items cleared"))
            }
            SyncEvent.NavigateBack -> viewModelScope.launch {
                _effect.send(SyncEffect.NavigateBack)
            }
        }
    }
}