package com.example.gitphos.di

import android.content.Context
import androidx.room.Room
import com.example.gitphos.data.local.db.GitPhosDatabase
import com.example.gitphos.data.local.db.dao.RepoMetadataDao
import com.example.gitphos.data.local.db.dao.SyncHistoryDao
import com.example.gitphos.data.local.db.dao.UploadQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GitPhosDatabase =
        Room.databaseBuilder(
            context,
            GitPhosDatabase::class.java,
            "gitphos.db"
        ).build()

    @Provides
    fun provideUploadQueueDao(db: GitPhosDatabase): UploadQueueDao = db.uploadQueueDao()

    @Provides
    fun provideSyncHistoryDao(db: GitPhosDatabase): SyncHistoryDao = db.syncHistoryDao()

    @Provides
    fun provideRepoMetadataDao(db: GitPhosDatabase): RepoMetadataDao = db.repoMetadataDao()
}