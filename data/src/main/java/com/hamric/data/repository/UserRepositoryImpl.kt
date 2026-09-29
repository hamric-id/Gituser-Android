package com.hamric.data.repository


import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.UserDao
import com.hamric.core.network.GitHubApi
import com.hamric.data.mapper.toDomain
import com.hamric.data.mapper.toEntity
import com.hamric.domain.model.User
import com.hamric.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val api: GitHubApi,
    private val userDao: UserDao,
    private val dispatchers: DispatchersProvider
) : UserRepository {

    override suspend fun searchUsers(query: String): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                val response = api.searchUsers(query = query)
                userDao.insertAll(response.items.map { it.toEntity() })
                Resource.Success(Unit)
            } catch (t: Throwable) {
                Resource.Error(t)
            }
        }

    override fun observeCachedUsers(query: String): Flow<List<User>> =
        userDao.searchCached(query).map { list -> list.map { it.toDomain() } }
}