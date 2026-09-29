package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.repository.UserRepository

class FetchUserListPageUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(since: Long): Resource<Long> =
        repository.fetchUserListPage(since)
}