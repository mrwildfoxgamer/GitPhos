package com.example.gitphos.ui.sync

import com.example.gitphos.data.local.db.entity.UploadQueueEntity

data class SyncState(
    val pendingItems: List<UploadQueueEntity> = emptyList(),
    val activeRepoName: String = "",
    val remoteUrl: String = "",
    val isSyncing: Boolean = false,
    val noActiveRepo: Boolean = false,
    val workStatus: WorkStatus = WorkStatus.Idle
)

sealed interface WorkStatus {
    data object Idle : WorkStatus
    data object Running : WorkStatus
    data class Success(val uploaded: Int) : WorkStatus
    data class PartialSuccess(val uploaded: Int, val failed: Int) : WorkStatus
    data class Failed(val reason: String) : WorkStatus
}

sealed interface SyncEvent {
    data object StartSync : SyncEvent
    data object CancelSync : SyncEvent
    data object ClearCompleted : SyncEvent
    data object NavigateBack : SyncEvent
}

sealed interface SyncEffect {
    data object NavigateBack : SyncEffect
    data class ShowMessage(val message: String) : SyncEffect
}