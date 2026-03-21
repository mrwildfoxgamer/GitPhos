package com.example.gitphos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.gitphos.domain.model.SyncResult
import com.example.gitphos.domain.usecase.SyncUploadQueueUseCase
import com.example.gitphos.data.local.datastore.PrefsDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncUploadQueueUseCase: SyncUploadQueueUseCase,
    private val prefsDataStore: PrefsDataStore,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_REMOTE_URL = "remote_url"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_UPLOADED_COUNT = "uploaded_count"
        const val KEY_FAILED_COUNT = "failed_count"
        const val WORK_NAME = "gitphos_sync_worker"
    }

    override suspend fun doWork(): Result {
        val remoteUrl = inputData.getString(KEY_REMOTE_URL)
            ?: return Result.failure(workDataOf(KEY_ERROR_MESSAGE to "Missing remote URL"))

        val localRepoPath = prefsDataStore.getActiveRepoPath()
            ?: return Result.failure(workDataOf(KEY_ERROR_MESSAGE to "No active repo path set"))

        Timber.d("SyncWorker: starting sync — repo=$localRepoPath remote=$remoteUrl")

        return when (val result = syncUploadQueueUseCase(localRepoPath, remoteUrl)) {
            is SyncResult.Success -> {
                Timber.d("SyncWorker: sync complete")
                Result.success()
            }
            is SyncResult.PartialSuccess -> {
                Timber.w("SyncWorker: partial — uploaded=${result.uploaded} failed=${result.failed}")
                Result.success(
                    workDataOf(
                        KEY_UPLOADED_COUNT to result.uploaded,
                        KEY_FAILED_COUNT to result.failed
                    )
                )
            }
            is SyncResult.Failure -> {
                Timber.e("SyncWorker: failed — ${result.reason}")
                if (runAttemptCount < 3) Result.retry()
                else Result.failure(workDataOf(KEY_ERROR_MESSAGE to result.reason))
            }
        }
    }
}