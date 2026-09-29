package com.hamric.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_details")
data class UserDetailEntity(
    @PrimaryKey val login: String,
    val id: Long,
    val avatarUrl: String,
    val htmlUrl: String,
    val name: String?,
    val company: String?,
    val blog: String?,
    val location: String?,
    val bio: String?,
    val twitterUsername: String?,
    val publicRepos: Int,
    val publicGists: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val cachedAt: Long,
    val lastAccessedAt: Long
)