package com.hamric.gituser.di

import com.hamric.core.common.DefaultDispatchersProvider
import com.hamric.core.common.DispatchersProvider
import com.hamric.domain.usecase.FetchUserListPageUseCase
import com.hamric.domain.usecase.GetUserDetailUseCase
import com.hamric.domain.usecase.ObserveAllCachedUsersUseCase
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import org.koin.dsl.module

val appModule = module {
    single<DispatchersProvider> { DefaultDispatchersProvider() }

    factory { SearchUsersUseCase(get()) }
    factory { ObserveCachedUsersUseCase(get()) }
    factory { FetchUserListPageUseCase(get()) }
    factory { ObserveAllCachedUsersUseCase(get()) }

    factory { GetUserDetailUseCase(get()) }
}