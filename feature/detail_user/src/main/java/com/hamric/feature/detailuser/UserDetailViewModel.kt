package com.hamric.feature.detailuser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.core.common.Resource
import com.hamric.domain.repository.UserDetailRepository
import com.hamric.domain.usecase.GetUserDetailUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val getUserDetail: GetUserDetailUseCase,
    private val userDetailRepository: UserDetailRepository
) : ViewModel() {

    private val _state = MutableLiveData(UserDetailUiState())
    val state: LiveData<UserDetailUiState> = _state

    private var observeJob: Job? = null
    private var currentLogin: String? = null

    fun load(login: String) {
        if (login.isBlank()) return
        if (currentLogin == login) return

        currentLogin = login

        observeCached(login)
        fetchAndCache(login)
    }

    fun retry() {
        val login = currentLogin ?: return
        fetchAndCache(login)
    }

    private fun observeCached(login: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            userDetailRepository.observeCachedDetail(login).collect { cached ->
                if (cached != null) {
                    updateState { it.copy(user = cached, isLoading = false) }
                    userDetailRepository.touch(login)
                }
            }
        }
    }

    private fun fetchAndCache(login: String) {
        viewModelScope.launch {
            if (_state.value?.user == null) {
                updateState { it.copy(isLoading = true, error = null) }
            } else {
                updateState { it.copy(error = null) }
            }

            when (val result = getUserDetail(login)) {
                is Resource.Success -> updateState {
                    it.copy(
                        user = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> updateState {
                    it.copy(
                        isLoading = false,
                        error = result.throwable.message
                            ?: "Couldn't load profile"
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private inline fun updateState(block: (UserDetailUiState) -> UserDetailUiState) {
        val current = _state.value ?: UserDetailUiState()
        _state.value = block(current)
    }
}