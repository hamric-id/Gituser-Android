package com.hamric.feature.detailuser

import com.hamric.domain.model.UserDetail

data class UserDetailUiState(
    val user: UserDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)