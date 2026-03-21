package com.example.gitphos.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.first
private val Context.dataStore by preferencesDataStore(name = "gitphos_prefs")

class PrefsDataStore @Inject constructor(
    private val context: Context
) {

    val userPrefs: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        UserPrefs(
            authToken = prefs[PrefsKeys.AUTH_TOKEN] ?: "",
            githubUsername = prefs[PrefsKeys.GITHUB_USERNAME] ?: "",
            activeRepoPath = prefs[PrefsKeys.ACTIVE_REPO_PATH] ?: "",
            activeRepoId = prefs[PrefsKeys.ACTIVE_REPO_ID] ?: -1L,
            autoSyncEnabled = prefs[PrefsKeys.AUTO_SYNC_ENABLED] ?: false,
            syncIntervalMins = prefs[PrefsKeys.SYNC_INTERVAL_MINS] ?: 30L,
            theme = prefs[PrefsKeys.THEME] ?: "SYSTEM"
        )
    }

    suspend fun setAuthToken(token: String) {
        context.dataStore.edit { it[PrefsKeys.AUTH_TOKEN] = token }
    }

    suspend fun setGithubUsername(username: String) {
        context.dataStore.edit { it[PrefsKeys.GITHUB_USERNAME] = username }
    }

    suspend fun setActiveRepo(id: Long, path: String) {
        context.dataStore.edit {
            it[PrefsKeys.ACTIVE_REPO_ID] = id
            it[PrefsKeys.ACTIVE_REPO_PATH] = path
        }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { it[PrefsKeys.AUTO_SYNC_ENABLED] = enabled }
    }

    suspend fun setSyncInterval(minutes: Long) {
        context.dataStore.edit { it[PrefsKeys.SYNC_INTERVAL_MINS] = minutes }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[PrefsKeys.THEME] = theme }
    }

    suspend fun clearAuth() {
        context.dataStore.edit {
            it.remove(PrefsKeys.AUTH_TOKEN)
            it.remove(PrefsKeys.GITHUB_USERNAME)
        }
    }

    suspend fun getActiveRepoPath(): String? {
        // Added '?' before .ifEmpty
        return context.dataStore.data.map { it[PrefsKeys.ACTIVE_REPO_PATH] }.first()
            ?.ifEmpty { null }
    }

    suspend fun getStoredToken(): String? {
        // Added '?' before .ifEmpty
        return context.dataStore.data.map { it[PrefsKeys.AUTH_TOKEN] }.first()?.ifEmpty { null }
    }
}