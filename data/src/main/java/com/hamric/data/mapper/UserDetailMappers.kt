package com.hamric.data.mapper

import com.hamric.core.database.UserDetailEntity
import com.hamric.core.network.UserDetailDto
import com.hamric.domain.model.UserDetail

fun UserDetailDto.toDomain(): UserDetail = UserDetail(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserDetailDto.toEntity(now: Long): UserDetailEntity = UserDetailEntity(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cachedAt = now,
    lastAccessedAt = now
)

fun UserDetailEntity.toDomain(): UserDetail = UserDetail(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt
)