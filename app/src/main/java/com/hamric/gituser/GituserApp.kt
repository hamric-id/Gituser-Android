package com.hamric.gituser

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GituserApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GituserApp)
            modules(appModule)
        }
    }
}