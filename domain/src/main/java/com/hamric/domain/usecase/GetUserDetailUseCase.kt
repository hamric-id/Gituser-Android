package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.model.UserDetail
import com.hamric.domain.repository.UserDetailRepository

class GetUserDetailUseCase(private val repository: UserDetailRepository) {
    suspend operator fun invoke(login: String): Resource<UserDetail> {
        if (login.isBlank()) {
            return Resource.Error(IllegalArgumentException("login is blank"))
        }
        return repository.fetchAndCacheDetail(login.trim())
    }
}