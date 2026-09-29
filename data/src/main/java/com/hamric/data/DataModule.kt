package com.hamric.data

import com.hamric.data.repository.UserDetailRepositoryImpl
import com.hamric.data.repository.UserRepositoryImpl
import com.hamric.domain.repository.UserDetailRepository
import com.hamric.domain.repository.UserRepository
import org.koin.dsl.module

val dataModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get(), get(), get()) }
    single<UserDetailRepository> {
        UserDetailRepositoryImpl(
            api = get(),
            userDetailDao = get(),
            dispatchers = get()
        )
    }
}