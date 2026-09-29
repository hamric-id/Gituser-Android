package com.hamric.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDetailDao {

    @Query("SELECT * FROM user_details WHERE login = :login LIMIT 1")
    fun observe(login: String): Flow<UserDetailEntity?>

    @Query("SELECT * FROM user_details WHERE login = :login LIMIT 1")
    suspend fun get(login: String): UserDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserDetailEntity)

    @Query("UPDATE user_details SET lastAccessedAt = :timestamp WHERE login = :login")
    suspend fun touch(login: String, timestamp: Long)

    @Query(
        """
        DELETE FROM user_details
        WHERE login NOT IN (
            SELECT login FROM user_details
            ORDER BY lastAccessedAt DESC
            LIMIT :maxEntries
        )
        """
    )
    suspend fun trimTo(maxEntries: Int)
}