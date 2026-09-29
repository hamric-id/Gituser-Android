package com.hamric.data.repository

import app.cash.turbine.test
import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.PaginationDao
import com.hamric.core.database.PaginationEntity
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
    private lateinit var userDao: UserDao
    private lateinit var paginationDao: PaginationDao
    private lateinit var dispatchers: DispatchersProvider
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        api = mockk()
        userDao = mockk(relaxed = true)
        paginationDao = mockk(relaxed = true)
        dispatchers = object : DispatchersProvider {
            override val io = dispatcher
            override val default = dispatcher
            override val main = dispatcher
        }
        repository = UserRepositoryImpl(api, userDao, paginationDao,dispatchers)
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
        coEvery { userDao.insertAll(any()) } returns Unit

        val result = repository.searchUsers("octocat",page=1)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { userDao.insertAll(any()) }
    }

    @Test
    fun `searchUsers failure returns Error and does not insert`() = runTest(dispatcher) {
        val boom = RuntimeException("net down")
        coEvery { api.searchUsers(any()) } throws boom

        val result = repository.searchUsers("octocat",page=1)

        assertTrue(result is Resource.Error)
        assertEquals(boom, (result as Resource.Error).throwable)
        coVerify(exactly = 0) { userDao.insertAll(any()) }
    }

    @Test
    fun `observeCachedUsers maps entities to domain`() = runTest(dispatcher) {
        val entities = listOf(
            UserEntity(1L, "octocat", "https://avatar/1", "https://html/1")
        )
        every { userDao.searchCached("octo") } returns flowOf(entities)

        repository.observeCachedUsers("octo").test {
            val users = awaitItem()
            assertEquals(1, users.size)
            assertEquals("octocat", users.first().login)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchUserListPage success inserts users and returns max id as new cursor`() =
        runTest(dispatcher) {
            val users = listOf(
                UserDto(10L, "a", "a", "a"),
                UserDto(25L, "b", "b", "b"),
                UserDto(42L, "c", "c", "c")
            )
            coEvery { api.listUsers(0L, any()) } returns users

            val result = repository.fetchUserListPage(since = 0L)

            assertTrue(result is Resource.Success)
            assertEquals(42L, (result as Resource.Success).data)
            coVerify(exactly = 1) { userDao.insertAll(any()) }
            coVerify(exactly = 1) {
                paginationDao.upsert(
                    PaginationEntity(PaginationEntity.KEY_LIST_USERS, 42L)
                )
            }
        }

    @Test
    fun `fetchUserListPage empty response keeps same cursor and does not insert`() =
        runTest(dispatcher) {
            coEvery { api.listUsers(50L, any()) } returns emptyList()

            val result = repository.fetchUserListPage(since = 50L)

            assertTrue(result is Resource.Success)
            assertEquals(50L, (result as Resource.Success).data)
            coVerify(exactly = 0) { userDao.insertAll(any()) }
            coVerify(exactly = 0) { paginationDao.upsert(any()) }
        }

    @Test
    fun `fetchUserListPage error returns Error and does not touch DB`() =
        runTest(dispatcher) {
            val boom = RuntimeException("net down")
            coEvery { api.listUsers(any(), any()) } throws boom

            val result = repository.fetchUserListPage(since = 0L)

            assertTrue(result is Resource.Error)
            coVerify(exactly = 0) { userDao.insertAll(any()) }
            coVerify(exactly = 0) { paginationDao.upsert(any()) }
        }

    @Test
    fun `observeAllCachedUsers maps entities to domain`() = runTest(dispatcher) {
        val entities = listOf(
            UserEntity(1L, "octocat", "a", "h"),
            UserEntity(2L, "torvalds", "a", "h")
        )
        every { userDao.observeAll() } returns flowOf(entities)

        repository.observeAllCachedUsers().test {
            val users = awaitItem()
            assertEquals(2, users.size)
            assertEquals("octocat", users.first().login)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lastListSince returns stored value when present`() = runTest(dispatcher) {
        coEvery { paginationDao.get(PaginationEntity.KEY_LIST_USERS) } returns
                PaginationEntity(PaginationEntity.KEY_LIST_USERS, 12345L)

        val since = repository.lastListSince()

        assertEquals(12345L, since)
    }

    @Test
    fun `lastListSince returns 0 when no cursor stored`() = runTest(dispatcher) {
        coEvery { paginationDao.get(PaginationEntity.KEY_LIST_USERS) } returns null

        val since = repository.lastListSince()

        assertEquals(0L, since)
    }
}