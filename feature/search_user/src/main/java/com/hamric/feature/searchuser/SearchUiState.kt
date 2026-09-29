package com.hamric.feature.searchuser

import com.hamric.domain.model.User

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false
)