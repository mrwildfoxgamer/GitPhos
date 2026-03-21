package com.example.gitphos.domain.usecase

import com.example.gitphos.domain.model.GitErrorCode
import com.example.gitphos.domain.model.GitResult
import com.example.gitphos.domain.model.SyncResult
import com.example.gitphos.domain.repository.AuthRepository
import com.example.gitphos.domain.repository.GitRepository
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.data.local.db.dao.SyncHistoryDao
import com.example.gitphos.data.local.db.entity.UploadQueueEntity
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SyncUploadQueueUseCase @Inject constructor(
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
            val sourceFile = File(item.filePath)
            if (!sourceFile.exists()) {
                uploadQueueDao.markFailed(item.id, "File not found")
                return false
            }

            val destFile = File(localRepoPath, sourceFile.name)
            sourceFile.copyTo(destFile, overwrite = true)

            uploadQueueDao.markInProgress(item.id)

            val addResult = gitRepository.addFiles(localRepoPath, listOf(sourceFile.name))
            if (addResult is GitResult.Error) {
                uploadQueueDao.markFailed(item.id, addResult.message)
                return false
            }

            val commitResult = gitRepository.commit(
                localPath = localRepoPath,
                message = "Upload ${sourceFile.name}",
                authorName = "GitPhos",
                authorEmail = "gitphos@local"
            )
            if (commitResult is GitResult.Error) {
                uploadQueueDao.markFailed(item.id, commitResult.message)
                return false
            }

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