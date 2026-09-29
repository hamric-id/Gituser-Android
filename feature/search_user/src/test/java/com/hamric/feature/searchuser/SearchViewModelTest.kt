package com.hamric.feature.searchuser

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hamric.core.common.Resource
import com.hamric.domain.model.User
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var observeCached: ObserveCachedUsersUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        searchUsers = mockk()
        observeCached = mockk()

        every { observeCached(any()) } returns flowOf(
            listOf(User(1L, "octocat", "url", "html"))
        )
        coEvery { searchUsers(any()) } returns Resource.Success(Unit)

        viewModel = SearchViewModel(searchUsers, observeCached)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `onQueryChange updates query in state`() = runTest(dispatcher) {
        viewModel.onQueryChange("octo")
        assertEquals("octo", viewModel.state.value?.query)
    }

    @Test
    fun `blank query does not trigger search`() = runTest(dispatcher) {
        viewModel.onQueryChange("")
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 0) { searchUsers(any()) }
    }

    @Test
    fun `successful search populates users`() = runTest(dispatcher) {
        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertFalse(state.users.isEmpty())
        assertEquals("octocat", state.users.first().login)
        assertNull(state.error)
    }

    @Test
    fun `search error sets error in state`() = runTest(dispatcher) {
        every { observeCached(any()) } returns flowOf(emptyList())
        coEvery { searchUsers(any()) } returns Resource.Error(RuntimeException("boom"))

        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("boom", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `empty result sets isEmpty flag`() = runTest(dispatcher) {
        every { observeCached(any()) } returns flowOf(emptyList())
        coEvery { searchUsers(any()) } returns Resource.Success(Unit)

        viewModel.onQueryChange("zzzz")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertTrue(state.isEmpty)
        assertTrue(state.users.isEmpty())
    }

    @Test
    fun `retry triggers search again`() = runTest(dispatcher) {
        viewModel.onQueryChange("octo")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        io.mockk.coVerify(atLeast = 2) { searchUsers(any()) }
    }
}