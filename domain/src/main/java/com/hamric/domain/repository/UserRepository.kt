package com.hamric.domain.repository

import com.hamric.core.common.Resource
import com.hamric.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun searchUsers(query: String): Resource<Unit>
    fun observeCachedUsers(query: String): Flow<List<User>>
}