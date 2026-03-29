package com.example.gitphos.domain.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.gitphos.domain.model.GitResult
import com.example.gitphos.domain.model.SyncResult
import com.example.gitphos.domain.repository.AuthRepository
import com.example.gitphos.domain.repository.GitRepository
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.data.local.db.dao.SyncHistoryDao
import com.example.gitphos.data.local.db.entity.UploadQueueEntity
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SyncUploadQueueUseCase @Inject constructor(
    @ApplicationContext private val context: Context, // Added Context to resolve URIs
    private val uploadQueueDao: UploadQueueDao,
    private val syncHistoryDao: SyncHistoryDao,
    private val gitRepository: GitRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(localRepoPath: String, remoteUrl: String): SyncResult =
        withContext(Dispatchers.IO) {
            val token = authRepository.getStoredToken()
                ?: return@withContext SyncResult.Failure("No auth token found")

            val pending = uploadQueueDao.getPendingItems()
            if (pending.isEmpty()) return@withContext SyncResult.Success

            val initResult = gitRepository.initRepository(localRepoPath)
            if (initResult is GitResult.Error) {
                return@withContext SyncResult.Failure(initResult.message, initResult.cause)
            }

            gitRepository.ensureBranch(localRepoPath)

            var uploaded = 0
            var failed = 0

            pending.forEach { item ->
                val result = processItem(item, localRepoPath, remoteUrl, token)
                if (result) uploaded++ else failed++
            }

            if (uploaded > 0) {
                recordHistory(localRepoPath, uploaded, failed)
            }

            return@withContext when {
                failed == 0 -> SyncResult.Success
                uploaded == 0 -> SyncResult.Failure("All $failed items failed")
                else -> SyncResult.PartialSuccess(uploaded, failed)
            }
        }

    private suspend fun processItem(
        item: UploadQueueEntity,
        localRepoPath: String,
        remoteUrl: String,
        token: String
    ): Boolean {
        return try {
            val uri = Uri.parse(item.filePath)
            val fileName = getFileNameFromUri(context, uri) ?: "upload_${System.currentTimeMillis()}.jpg"
            val destFile = File(localRepoPath, fileName)

            // 1. Open InputStream from the Content URI and copy it to the local Git repo
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: run {
                    uploadQueueDao.markFailed(item.id, "Cannot access original file")
                    return false
                }
            } catch (e: Exception) {
                uploadQueueDao.markFailed(item.id, "Failed to copy file: ${e.message}")
                return false
            }

            uploadQueueDao.markInProgress(item.id)

            // 2. Add to Git
            val addResult = gitRepository.addFiles(localRepoPath, listOf(fileName))
            if (addResult is GitResult.Error) {
                uploadQueueDao.markFailed(item.id, addResult.message)
                return false
            }

            // 3. Commit
            val commitResult = gitRepository.commit(
                localPath = localRepoPath,
                message = "Upload $fileName",
                authorName = "GitPhos",
                authorEmail = "gitphos@local"
            )
            if (commitResult is GitResult.Error) {
                uploadQueueDao.markFailed(item.id, commitResult.message)
                return false
            }

            // 4. Push
            val pushResult = gitRepository.push(localRepoPath, remoteUrl, token)
            if (pushResult is GitResult.Error) {
                uploadQueueDao.markFailed(item.id, pushResult.message)
                return false
            }

            uploadQueueDao.markCompleted(item.id)
            true
        } catch (e: Exception) {
            uploadQueueDao.markFailed(item.id, e.message ?: "Unknown error")
            false
        }
    }

    // Helper function to extract the real filename from a Content URI
    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { File(it).name }
        }
        return result
    }

    private suspend fun recordHistory(repoPath: String, uploaded: Int, failed: Int) {
        syncHistoryDao.insert(
            SyncHistoryEntity(
                repoId = 0L, // update when repo selection is implemented
                commitHash = null,
                filesChanged = uploaded,
                status = if (failed == 0) "SUCCESS" else "PARTIAL",
                triggeredBy = "AUTO",
                errorMessage = if (failed > 0) "$failed files failed" else null
            )
        )
    }
}