package com.hamric.feature.searchuser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.core.common.Resource
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUsers: SearchUsersUseCase,
    private val observeCached: ObserveCachedUsersUseCase
) : ViewModel() {

    private val _state = MutableLiveData(SearchUiState())
    val state: LiveData<SearchUiState> = _state

    private val queryFlow = MutableStateFlow("")
    private var debounceJob: Job? = null
    private var observeJob: Job? = null

    fun onQueryChange(newQuery: String) {
        updateState { it.copy(query = newQuery, error = null) }
        queryFlow.value = newQuery
        observeCachedQuery(newQuery)
        debounceSearch()
    }

    fun retry() {
        val q = _state.value?.query.orEmpty()
        if (q.isNotBlank()) runSearch(q)
    }

    private fun debounceSearch() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { q -> runSearch(q) }
        }
    }

    private fun observeCachedQuery(query: String) {
        observeJob?.cancel()
        if (query.isBlank()) {
            updateState { it.copy(users = emptyList(), isEmpty = false) }
            return
        }
        observeJob = viewModelScope.launch {
            observeCached(query).collect { users ->
                val loading = _state.value?.isLoading == true
                updateState {
                    it.copy(
                        users = users,
                        isEmpty = users.isEmpty() && !loading && it.query.isNotBlank()
                    )
                }
            }
        }
    }

    private fun runSearch(query: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val result = searchUsers(query)) {
                is Resource.Success -> updateState { it.copy(isLoading = false) }
                is Resource.Error -> updateState {
                    it.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Something went wrong"
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private inline fun updateState(block: (SearchUiState) -> SearchUiState) {
        val current = _state.value ?: SearchUiState()
        _state.value = block(current)
    }
}