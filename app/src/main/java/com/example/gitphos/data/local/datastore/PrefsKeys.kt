package com.example.gitphos.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrefsKeys {
    val AUTH_TOKEN         = stringPreferencesKey("auth_token")
    val GITHUB_USERNAME    = stringPreferencesKey("github_username")
    val ACTIVE_REPO_PATH   = stringPreferencesKey("active_repo_path")
    val ACTIVE_REPO_ID     = longPreferencesKey("active_repo_id")
    val AUTO_SYNC_ENABLED  = booleanPreferencesKey("auto_sync_enabled")
    val SYNC_INTERVAL_MINS = longPreferencesKey("sync_interval_mins")
    val THEME              = stringPreferencesKey("theme") // LIGHT | DARK | SYSTEM
}