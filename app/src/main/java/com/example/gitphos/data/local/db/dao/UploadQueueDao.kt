package com.example.gitphos.data.local.db.dao

import androidx.room.*
import com.example.gitphos.data.local.db.entity.UploadQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UploadQueueEntity): Long

    @Update
    suspend fun update(item: UploadQueueEntity)

    @Delete
    suspend fun delete(item: UploadQueueEntity)

    @Query("SELECT * FROM upload_queue WHERE repoId = :repoId ORDER BY addedAt ASC")
    fun observeByRepo(repoId: Long): Flow<List<UploadQueueEntity>>

    @Query("SELECT * FROM upload_queue WHERE status = 'PENDING' ORDER BY addedAt ASC")
    suspend fun getPending(): List<UploadQueueEntity>

    @Query("UPDATE upload_queue SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, error: String? = null)

    @Query("UPDATE upload_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("DELETE FROM upload_queue WHERE status = 'DONE'")
    suspend fun clearCompleted()
}