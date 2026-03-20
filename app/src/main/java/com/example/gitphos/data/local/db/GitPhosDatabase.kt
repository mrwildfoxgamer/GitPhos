package com.example.gitphos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.dao.SyncHistoryDao
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity
import com.example.gitphos.data.local.db.entity.UploadQueueEntity

@Database(
    entities = [
        UploadQueueEntity::class,
        SyncHistoryEntity::class,
        RepoMetadataEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GitPhosDatabase : RoomDatabase() {
    abstract fun uploadQueueDao(): UploadQueueDao
    abstract fun syncHistoryDao(): SyncHistoryDao
    abstract fun repoMetadataDao(): RepoMetadataDao
}