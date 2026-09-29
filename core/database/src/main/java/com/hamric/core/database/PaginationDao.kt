package com.hamric.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaginationDao {

    @Query("SELECT * FROM pagination_state WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): PaginationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PaginationEntity)
}