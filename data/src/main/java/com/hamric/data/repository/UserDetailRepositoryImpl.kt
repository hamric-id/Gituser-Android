package com.hamric.data.repository

import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.UserDetailDao
import com.hamric.core.network.GitHubApi
import com.hamric.data.mapper.toDomain
import com.hamric.data.mapper.toEntity
import com.hamric.domain.model.UserDetail
import com.hamric.domain.repository.UserDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserDetailRepositoryImpl(
    private val api: GitHubApi,
    private val userDetailDao: UserDetailDao,
    private val dispatchers: DispatchersProvider,
    private val maxCacheSize: Int = MAX_CACHED_DETAILS
) : UserDetailRepository {

    override fun observeCachedDetail(login: String): Flow<UserDetail?> =
        userDetailDao.observe(login).map { it?.toDomain() }

    override suspend fun fetchAndCacheDetail(login: String): Resource<UserDetail> =
        withContext(dispatchers.io) {
            try {
                val dto = api.getUserDetail(login)
                val now = System.currentTimeMillis()
                userDetailDao.upsert(dto.toEntity(now))
                userDetailDao.trimTo(maxCacheSize)
                Resource.Success(dto.toDomain())
            } catch (t: Throwable) {
                Resource.Error(t)
            }
        }

    override suspend fun touch(login: String) =
        withContext(dispatchers.io) {
            userDetailDao.touch(login, System.currentTimeMillis())
        }

    companion object {

        const val MAX_CACHED_DETAILS = 200
    }
}