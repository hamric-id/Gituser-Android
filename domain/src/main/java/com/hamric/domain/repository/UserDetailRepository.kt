package com.hamric.domain.repository

import com.hamric.core.common.Resource
import com.hamric.domain.model.UserDetail
import kotlinx.coroutines.flow.Flow

interface UserDetailRepository {
    fun observeCachedDetail(login: String): Flow<UserDetail?>

    suspend fun fetchAndCacheDetail(login: String): Resource<UserDetail>

    suspend fun touch(login: String)
}