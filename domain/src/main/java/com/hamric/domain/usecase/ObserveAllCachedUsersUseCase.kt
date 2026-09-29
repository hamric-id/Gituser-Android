package com.hamric.domain.usecase

import com.hamric.domain.model.User
import com.hamric.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllCachedUsersUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<List<User>> = repository.observeAllCachedUsers()
}