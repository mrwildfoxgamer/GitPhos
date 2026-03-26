package com.example.gitphos.ui.picker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.data.local.db.entity.UploadQueueEntity
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
class PickerViewModel @Inject constructor(
    private val uploadQueueDao: UploadQueueDao,
    private val repoMetadataDao: RepoMetadataDao
) : ViewModel() {

    private val _state = MutableStateFlow(PickerState())
    val state: StateFlow<PickerState> = _state.asStateFlow()

    private val _effect = Channel<PickerEffect>()
    val effect = _effect.receiveAsFlow()

    // Cache existing file paths to detect duplicates
    private var existingPaths: Set<String> = emptySet()
    private var activeRepoId: Long? = null

    init {
        viewModelScope.launch {
            val activeRepo = repoMetadataDao.getActiveRepo()
            if (activeRepo == null) {
                _state.update { it.copy(noActiveRepo = true) }
            } else {
                activeRepoId = activeRepo.id
                existingPaths = uploadQueueDao
                    .getPendingItems()
                    .filter { it.repoId == activeRepo.id }
                    .map { it.filePath }
                    .toSet()
            }
        }
    }

    fun onEvent(event: PickerEvent) {
        when (event) {
            is PickerEvent.ImagesSelected -> {
                val newUris = event.uris.filter { uri ->
                    uri.toString() !in existingPaths &&
                            uri !in _state.value.selectedUris
                }
                _state.update { it.copy(selectedUris = it.selectedUris + newUris) }
            }
            is PickerEvent.RemoveImage -> {
                _state.update { it.copy(selectedUris = it.selectedUris - event.uri) }
            }
            PickerEvent.ConfirmAdd -> addToQueue()
            PickerEvent.NavigateBack -> viewModelScope.launch {
                _effect.send(PickerEffect.NavigateBack)
            }
        }
    }

    private fun addToQueue() {
        val repoId = activeRepoId ?: return
        val uris = _state.value.selectedUris
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isAdding = true) }
            uris.forEach { uri ->
                uploadQueueDao.insert(
                    UploadQueueEntity(
                        repoId = repoId,
                        filePath = uri.toString(),
                        status = "PENDING"
                    )
                )
            }
            _state.update { it.copy(isAdding = false, selectedUris = emptyList()) }
            _effect.send(PickerEffect.ShowMessage("${uris.size} image(s) added to queue"))
            _effect.send(PickerEffect.NavigateBack)
        }
    }
}