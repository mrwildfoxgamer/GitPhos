package com.example.gitphos.ui.repo

import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import com.example.gitphos.data.remote.model.GithubRepoDto

data class RepoState(
    val repos: List<RepoMetadataEntity> = emptyList(),
    val activeRepoId: Long = -1L,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddDialog: Boolean = false,
    val availableRemoteRepos: List<GithubRepoDto> = emptyList(),
    val isFetchingRepos: Boolean = false,
    val dialogName: String = "",
    val dialogRemoteUrl: String = "",
    val dialogLocalPath: String = "",
    val dialogBranch: String = "main",
    val dialogError: String? = null
)

sealed interface RepoEvent {
    data object ShowAddDialog : RepoEvent
    data object DismissDialog : RepoEvent
    data class DialogNameChanged(val value: String) : RepoEvent
    data class DialogRemoteUrlChanged(val value: String) : RepoEvent
    data class DialogLocalPathChanged(val value: String) : RepoEvent
    data class DialogBranchChanged(val value: String) : RepoEvent
    data object ConfirmAdd : RepoEvent
    data class SetActive(val repo: RepoMetadataEntity) : RepoEvent
    data class DeleteRepo(val repo: RepoMetadataEntity) : RepoEvent
    data object NavigateBack : RepoEvent
    data class RemoteRepoSelected(val repo: GithubRepoDto) : RepoEvent
}

sealed interface RepoEffect {
    data object NavigateBack : RepoEffect
    data class ShowMessage(val message: String) : RepoEffect
}