package com.hamric.gituser.di

import com.hamric.feature.detailuser.UserDetailViewModel
import com.hamric.feature.users.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SearchViewModel(
            searchUsers = get(),
            observeCachedUsers = get(),
            fetchUserListPage = get(),
            observeAllCachedUsers = get(),
            userRepository = get()
        )
    }

    viewModel {
        UserDetailViewModel(
            getUserDetail = get(),
            userDetailRepository = get()
        )
    }
}