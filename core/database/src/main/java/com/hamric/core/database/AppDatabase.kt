package com.hamric.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        PaginationEntity::class,
        UserDetailEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun paginationDao(): PaginationDao
    abstract fun userDetailDao(): UserDetailDao
}