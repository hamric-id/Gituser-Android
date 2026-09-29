package com.hamric.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "pagination_state")
data class PaginationEntity(
    @PrimaryKey val key: String,
    val lastSinceId: Long = 0L
) {
    companion object {
        const val KEY_LIST_USERS = "list_users"
    }
}