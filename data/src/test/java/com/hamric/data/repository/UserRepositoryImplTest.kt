package com.hamric.data.repository

import app.cash.turbine.test
import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.UserDao
import com.hamric.core.database.UserEntity
import com.hamric.core.network.GitHubApi
import com.hamric.core.network.SearchUsersResponseDto
import com.hamric.core.network.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var api: GitHubApi
    private lateinit var dao: UserDao
    private lateinit var dispatchers: DispatchersProvider
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        api = mockk()
        dao = mockk(relaxed = true)
        dispatchers = object : DispatchersProvider {
            override val io = dispatcher
            override val default = dispatcher
            override val main = dispatcher
        }
        repository = UserRepositoryImpl(api, dao, dispatchers)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `searchUsers success inserts into dao and returns Success`() = runTest(dispatcher) {
        val dto = UserDto(
            id = 1L,
            login = "octocat",
            avatarUrl = "https://avatar/1",
            htmlUrl = "https://html/1"
        )
        coEvery { api.searchUsers(any()) } returns SearchUsersResponseDto(
            totalCount = 1, incompleteResults = false, items = listOf(dto)
        )
        coEvery { dao.insertAll(any()) } returns Unit

        val result = repository.searchUsers("octocat")

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `searchUsers failure returns Error and does not insert`() = runTest(dispatcher) {
        val boom = RuntimeException("net down")
        coEvery { api.searchUsers(any()) } throws boom

        val result = repository.searchUsers("octocat")

        assertTrue(result is Resource.Error)
        assertEquals(boom, (result as Resource.Error).throwable)
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `observeCachedUsers maps entities to domain`() = runTest(dispatcher) {
        val entities = listOf(
            UserEntity(1L, "octocat", "https://avatar/1", "https://html/1")
        )
        every { dao.searchCached("octo") } returns flowOf(entities)

        repository.observeCachedUsers("octo").test {
            val users = awaitItem()
            assertEquals(1, users.size)
            assertEquals("octocat", users.first().login)
            cancelAndIgnoreRemainingEvents()
        }
    }
}