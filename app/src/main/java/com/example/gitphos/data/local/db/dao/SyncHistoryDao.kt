package com.example.gitphos.data.local.db.dao

import androidx.room.*
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SyncHistoryEntity): Long

    @Query("SELECT * FROM sync_history WHERE repoId = :repoId ORDER BY syncedAt DESC")
    fun observeByRepo(repoId: Long): Flow<List<SyncHistoryEntity>>

    @Query("SELECT * FROM sync_history WHERE repoId = :repoId ORDER BY syncedAt DESC LIMIT 1")
    suspend fun getLastSync(repoId: Long): SyncHistoryEntity?

    @Query("SELECT * FROM sync_history ORDER BY syncedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<SyncHistoryEntity>

    @Query("DELETE FROM sync_history WHERE repoId = :repoId")
    suspend fun clearForRepo(repoId: Long)
}