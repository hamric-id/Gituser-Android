package com.hamric.domain.usecase

import com.hamric.domain.model.User
import com.hamric.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCachedUsersUseCase(private val repository: UserRepository) {
    operator fun invoke(query: String): Flow<List<User>> = repository.observeCachedUsers(query)
}