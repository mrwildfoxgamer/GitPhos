package com.example.gitphos.di

import android.content.Context
import com.example.gitphos.data.local.datastore.PrefsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePrefsDataStore(
        @ApplicationContext context: Context
    ): PrefsDataStore = PrefsDataStore(context)
}