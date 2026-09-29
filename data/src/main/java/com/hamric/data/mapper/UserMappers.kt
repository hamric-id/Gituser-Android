package com.hamric.data.mapper

import com.hamric.core.database.UserEntity
import com.hamric.core.network.UserDto
import com.hamric.domain.model.User

fun UserDto.toEntity() = UserEntity(
    id = id, login = login, avatarUrl = avatarUrl, htmlUrl = htmlUrl
)

fun UserEntity.toDomain() = User(
    id = id, login = login, avatarUrl = avatarUrl, htmlUrl = htmlUrl
)