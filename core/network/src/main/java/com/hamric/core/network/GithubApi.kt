package com.hamric.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApi {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): SearchUsersResponseDto
}