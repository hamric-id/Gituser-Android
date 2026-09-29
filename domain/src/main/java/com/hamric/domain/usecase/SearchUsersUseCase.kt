package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.repository.UserRepository

class SearchUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(query: String): Resource<Unit> {
        if (query.isBlank()) return Resource.Success(Unit)
        return repository.searchUsers(query.trim())
    }
}