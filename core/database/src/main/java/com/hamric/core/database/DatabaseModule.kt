package com.hamric.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "github_users.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().userDao() }
}