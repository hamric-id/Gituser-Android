package com.hamric.gituser.di

import com.hamric.feature.searchuser.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
}