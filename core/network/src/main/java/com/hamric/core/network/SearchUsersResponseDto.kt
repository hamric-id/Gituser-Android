package com.hamric.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchUsersResponseDto(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "incomplete_results") val incompleteResults: Boolean,
    @Json(name = "items") val items: List<UserDto>
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: Long,
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val htmlUrl: String
)