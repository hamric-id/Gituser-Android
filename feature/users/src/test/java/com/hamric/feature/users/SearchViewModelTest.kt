package com.hamric.feature.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hamric.core.common.Resource
import com.hamric.domain.model.User
import com.hamric.domain.repository.UserRepository
import com.hamric.domain.usecase.FetchUserListPageUseCase
import com.hamric.domain.usecase.ObserveAllCachedUsersUseCase
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private lateinit var searchUsers: SearchUsersUseCase
    private lateinit var observeCachedUsers: ObserveCachedUsersUseCase
    private lateinit var fetchUserListPage: FetchUserListPageUseCase
    private lateinit var observeAllCachedUsers: ObserveAllCachedUsersUseCase
    private lateinit var userRepository: UserRepository

    private val cachedSearch = MutableStateFlow<List<User>>(emptyList())
    private val cachedList = MutableStateFlow<List<User>>(emptyList())
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        searchUsers = mockk()
        observeCachedUsers = mockk()
        fetchUserListPage = mockk()
        observeAllCachedUsers = mockk()
        userRepository = mockk()

        coEvery { userRepository.lastListSince() } returns 0L

        every { observeCachedUsers(any()) } returns cachedSearch
        every { observeAllCachedUsers() } returns cachedList

        coEvery { searchUsers(any(), any()) } returns Resource.Success(Unit)
        coEvery { fetchUserListPage(any()) } returns Resource.Success(0L)
    }

    private fun buildViewModel(): SearchViewModel = SearchViewModel(
        searchUsers = searchUsers,
        observeCachedUsers = observeCachedUsers,
        fetchUserListPage = fetchUserListPage,
        observeAllCachedUsers = observeAllCachedUsers,
        userRepository = userRepository
    )

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is LIST mode`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(SearchUiState.Mode.LIST, viewModel.state.value?.mode)
    }

    @Test
    fun `empty cache triggers initial fetch`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUserListPage(0L) }
    }

    @Test
    fun `existing cache does not trigger initial fetch`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { fetchUserListPage(any()) }
    }

    @Test
    fun `list emits into state`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "octocat", "a", "h"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals(1, state.users.size)
        assertEquals("octocat", state.users.first().login)
        assertEquals(SearchUiState.Mode.LIST, state.mode)
    }

    @Test
    fun `onListScrolledToEnd triggers next page fetch`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        coEvery { userRepository.lastListSince() } returns 30L
        coEvery { fetchUserListPage(30L) } returns Resource.Success(60L)
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onListScrolledToEnd()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUserListPage(30L) }
    }

    @Test
    fun `empty response sets endReached and stops further fetches`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        coEvery { userRepository.lastListSince() } returns 100L
        coEvery { fetchUserListPage(100L) } returns Resource.Success(100L)   // same cursor
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onListScrolledToEnd()
        advanceUntilIdle()

        assertTrue(viewModel.state.value!!.endReached)

        viewModel.onListScrolledToEnd()
        advanceUntilIdle()
        coVerify(exactly = 1) { fetchUserListPage(100L) }
    }

    @Test
    fun `typing query switches to SEARCH mode`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")

        assertEquals(SearchUiState.Mode.SEARCH, viewModel.state.value?.mode)
        assertEquals("octo", viewModel.state.value?.query)
    }


    @Test
    fun `blank query does not trigger search`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 0) { searchUsers(any()) }
    }

    @Test
    fun `successful search clears isSearching and shows cached results`() = runTest(dispatcher) {
        cachedSearch.value = listOf(User(1L, "octocat", "a", "h"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals(SearchUiState.Mode.SEARCH, state.mode)
        assertFalse(state.isSearching)
        assertEquals(1, state.users.size)
        assertNull(state.error)
    }

    @Test
    fun `search error sets error and clears isSearching`() = runTest(dispatcher) {
        coEvery { searchUsers(any(), any()) } returns Resource.Error(RuntimeException("boom"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("boom", state.error)
        assertFalse(state.isSearching)
    }

    @Test
    fun `clearing query returns to LIST mode`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals(SearchUiState.Mode.LIST, viewModel.state.value?.mode)
    }

    @Test
    fun `onSearchScrolledToEnd triggers next page`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.onSearchScrolledToEnd()
        advanceUntilIdle()

        coVerify(atLeast = 1) { searchUsers("kotlin", 2) }
    }

    @Test
    fun `retry in SEARCH mode reruns search`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 2) { searchUsers("octo", any()) }
    }

    @Test
    fun `retry in LIST mode fetches next page`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        coEvery { userRepository.lastListSince() } returns 30L
        coEvery { fetchUserListPage(30L) } returns Resource.Success(60L)
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUserListPage(30L) }
    }
}