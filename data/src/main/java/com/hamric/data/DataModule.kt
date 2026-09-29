package com.hamric.data

import com.hamric.data.repository.UserRepositoryImpl
import com.hamric.domain.repository.UserRepository
import org.koin.dsl.module

val dataModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
}