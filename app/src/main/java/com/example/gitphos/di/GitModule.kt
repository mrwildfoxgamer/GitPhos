// Make sure this package matches where you actually placed the file
package com.example.gitphos.di

// 1. Fix the imports to include '.example.'
import com.example.gitphos.data.git.GitRepositoryImpl
import com.example.gitphos.domain.repository.GitRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GitModule {

    @Binds
    @Singleton
    abstract fun bindGitRepository(impl: GitRepositoryImpl): GitRepository
}