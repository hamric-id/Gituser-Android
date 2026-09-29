package com.hamric.feature.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.core.common.Resource
import com.hamric.domain.repository.UserRepository
import com.hamric.domain.usecase.FetchUserListPageUseCase
import com.hamric.domain.usecase.ObserveAllCachedUsersUseCase
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
    private val observeCachedUsers: ObserveCachedUsersUseCase,
    private val fetchUserListPage: FetchUserListPageUseCase,
    private val observeAllCachedUsers: ObserveAllCachedUsersUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableLiveData(SearchUiState())
    val state: LiveData<SearchUiState> = _state

    private val queryFlow = MutableStateFlow("")

    private var listSince: Long = 0L
    private var searchPage: Int = 1
    private var searchQueryForPaging: String = ""
    private var listEndReached = false
    private var searchEndReached = false

    private var observeJob: Job? = null
    private var searchJob: Job? = null

    private var initialFetchTriggered = false

    private val _navigation = MutableLiveData<String?>()
    val navigation: LiveData<String?> = _navigation

    fun onUserClicked(login: String) {
        _navigation.value = login
    }

    fun onNavigationHandled() {
        _navigation.value = null
    }

    init {
        viewModelScope.launch {
            listSince = userRepository.lastListSince()
        }

        enterListMode()
        startSearchDebounce()
    }

    fun onQueryChange(newQuery: String) {
        val wasEmpty = _state.value?.query.isNullOrBlank()
        val isEmptyNow = newQuery.isBlank()

        updateState { it.copy(query = newQuery, error = null) }

        if (isEmptyNow) { // Switch to USER LIST mode
            if (!wasEmpty) {
                initialFetchTriggered = false
                listEndReached = false
                updateState {
                    it.copy(
                        mode = SearchUiState.Mode.LIST,
                        users = emptyList(),
                        isEmpty = false,
                        endReached = false
                    )
                }
                searchEndReached = false
                searchPage = 1
                searchQueryForPaging = ""
            }
            enterListMode()
        } else { // Switch to SEARCH mode / stay
            if (wasEmpty) {
                updateState {
                    it.copy(
                        mode = SearchUiState.Mode.SEARCH,
                        users = emptyList(),
                        isEmpty = false,
                        endReached = false
                    )
                }
                listEndReached = false
                observeJob?.cancel()
            }
            queryFlow.value = newQuery
        }
    }

    fun retry() {
        val s = _state.value ?: return
        if (s.mode == SearchUiState.Mode.SEARCH && s.query.isNotBlank()) {
            runSearch(s.query)
        } else {
            viewModelScope.launch { fetchNextListPage() }
        }
    }

    fun onListScrolledToEnd() {
        val s = _state.value ?: return
        if (s.mode != SearchUiState.Mode.LIST) return
        if (s.isLoadingMore || listEndReached) return
        viewModelScope.launch { fetchNextListPage() }
    }

    fun onSearchScrolledToEnd() {
        val s = _state.value ?: return
        if (s.mode != SearchUiState.Mode.SEARCH) return
        if (s.isLoadingMore || searchEndReached) return
        if (searchQueryForPaging.isBlank()) return
        viewModelScope.launch { fetchNextSearchPage() }
    }

    private fun enterListMode() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeAllCachedUsers().collect { users ->
                if (_state.value?.mode != SearchUiState.Mode.LIST) return@collect
                updateState {
                    it.copy(
                        users = users,
                        isEmpty = users.isEmpty() && !it.isLoadingMore && !listEndReached
                    )
                }

                if (!initialFetchTriggered && users.isEmpty() && !listEndReached) {
                    initialFetchTriggered = true
                    fetchNextListPage()
                }
            }
        }
    }

    private suspend fun fetchNextListPage() {
        if (_state.value?.isLoadingMore == true) return
        updateState { it.copy(isLoadingMore = true, error = null) }

        when (val result = fetchUserListPage(listSince)) {
            is Resource.Success -> {
                val newSince = result.data
                val madeProgress = newSince != listSince
                listSince = newSince
                if (!madeProgress) {
                    listEndReached = true
                }
                updateState {
                    it.copy(
                        isLoadingMore = false,
                        endReached = listEndReached
                    )
                }
            }
            is Resource.Error -> updateState {
                it.copy(
                    isLoadingMore = false,
                    error = result.throwable.message ?: "Couldn't load users"
                )
            }
            Resource.Loading -> Unit
        }
    }

    private fun startSearchDebounce() {
        searchJob = viewModelScope.launch {
            queryFlow
                .debounce(1000)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { q -> runSearch(q) }
        }
    }

    private fun runSearch(query: String) {
        searchPage = 1
        searchEndReached = false
        searchQueryForPaging = query

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeCachedUsers(query).collect { cached ->
                if (_state.value?.mode == SearchUiState.Mode.SEARCH) {
                    updateState {
                        it.copy(
                            users = cached,
                            isEmpty = cached.isEmpty()
                                    && !it.isSearching
                                    && !it.isLoadingMore
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            updateState { it.copy(isSearching = true, error = null) }
            when (val result = searchUsers(query, searchPage)) {
                is Resource.Success -> updateState { it.copy(isSearching = false) }
                is Resource.Error -> updateState {
                    it.copy(
                        isSearching = false,
                        error = result.throwable.message ?: "Bad Network — showing cached results"
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun fetchNextSearchPage() {
        if (_state.value?.isLoadingMore == true) return
        updateState { it.copy(isLoadingMore = true) }

        val next = searchPage + 1
        when (val result = searchUsers(searchQueryForPaging, next)) {
            is Resource.Success -> {
                searchPage = next
                updateState { it.copy(isLoadingMore = false) }
            }
            is Resource.Error -> updateState {
                it.copy(
                    isLoadingMore = false,
                    error = result.throwable.message ?: "Couldn't load more results"
                )
            }
            Resource.Loading -> Unit
        }
    }

    private inline fun updateState(block: (SearchUiState) -> SearchUiState) {
        val current = _state.value ?: SearchUiState()
        _state.value = block(current)
    }
}