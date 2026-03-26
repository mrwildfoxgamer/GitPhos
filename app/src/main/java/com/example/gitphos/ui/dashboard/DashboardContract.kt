package com.example.gitphos.ui.dashboard

import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity

data class DashboardState(
    val activeRepo: RepoMetadataEntity? = null,
    val pendingCount: Int = 0,
    val lastSync: SyncHistoryEntity? = null,
    val isLoading: Boolean = true
)

sealed interface DashboardEvent {
    data object SyncNow : DashboardEvent
    data object PickImages : DashboardEvent
    data object ManageRepo : DashboardEvent
    data object Logout : DashboardEvent
}

sealed interface DashboardEffect {
    data object NavigateToAuth : DashboardEffect
    data object NavigateToPicker : DashboardEffect
    data object NavigateToRepo : DashboardEffect
    data object NavigateToSync : DashboardEffect
    data class ShowError(val message: String) : DashboardEffect
}