package com.example.gitphos.data.local.db.dao

import androidx.room.*
import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repo: RepoMetadataEntity): Long

    @Update
    suspend fun update(repo: RepoMetadataEntity)

    @Delete
    suspend fun delete(repo: RepoMetadataEntity)

    @Query("SELECT * FROM repo_metadata ORDER BY name ASC")
    fun observeAll(): Flow<List<RepoMetadataEntity>>

    @Query("SELECT * FROM repo_metadata WHERE id = :id")
    suspend fun getById(id: Long): RepoMetadataEntity?

    @Query("SELECT * FROM repo_metadata WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveRepo(): RepoMetadataEntity?

    @Query("UPDATE repo_metadata SET isActive = 0")
    suspend fun clearActiveRepo()

    @Query("UPDATE repo_metadata SET isActive = 1 WHERE id = :id")
    suspend fun setActiveRepo(id: Long)

    @Query("UPDATE repo_metadata SET lastSyncAt = :time, totalCommits = :commits, diskUsageBytes = :bytes WHERE id = :id")
    suspend fun updateSyncStats(id: Long, time: Long, commits: Int, bytes: Long)
}