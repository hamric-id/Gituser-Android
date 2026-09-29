package com.hamric.gituser

import android.app.Application
import com.hamric.core.database.databaseModule
import com.hamric.core.network.networkModule
import com.hamric.data.dataModule
import com.hamric.gituser.di.appModule
import com.hamric.gituser.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GituserApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@GituserApp)
            modules(
                appModule,
                networkModule,
                databaseModule,
                dataModule,
                viewModelModule
            )
        }
    }
}