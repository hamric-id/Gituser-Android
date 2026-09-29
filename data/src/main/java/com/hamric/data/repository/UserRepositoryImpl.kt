package com.hamric.data.repository


import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.PaginationDao
import com.hamric.core.database.PaginationEntity
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
    private val paginationDao: PaginationDao,
    private val dispatchers: DispatchersProvider
) : UserRepository {

    override suspend fun searchUsers(query: String, page: Int): Resource<Unit> =
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

    override suspend fun fetchUserListPage(since: Long): Resource<Long> =
        withContext(dispatchers.io) {
            try {
                val users = api.listUsers(since = since)
                if (users.isEmpty()) {
                    return@withContext Resource.Success(since)
                }
                userDao.insertAll(users.map { it.toEntity() })

                val newSince = users.maxOf { it.id }
                paginationDao.upsert(
                    PaginationEntity(PaginationEntity.KEY_LIST_USERS, newSince)
                )
                Resource.Success(newSince)
            } catch (t: Throwable) {
                Resource.Error(t)
            }
        }

    override fun observeAllCachedUsers(): Flow<List<User>> =
        userDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun lastListSince(): Long =
        withContext(dispatchers.io) {
            paginationDao.get(PaginationEntity.KEY_LIST_USERS)?.lastSinceId ?: 0L
        }
}