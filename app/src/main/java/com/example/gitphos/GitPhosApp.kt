package com.example.gitphos

import android.app.Application
import com.fox.gitphos.BuildConfig
import timber.log.Timber


class GitPhosApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
