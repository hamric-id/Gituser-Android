package com.hamric.data.repository

import app.cash.turbine.test
import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.UserDetailDao
import com.hamric.core.database.UserDetailEntity
import com.hamric.core.network.GitHubApi
import com.hamric.core.network.UserDetailDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var api: GitHubApi
    private lateinit var dao: UserDetailDao
    private lateinit var dispatchers: DispatchersProvider
    private lateinit var repository: UserDetailRepositoryImpl

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
        repository = UserDetailRepositoryImpl(
            api = api,
            userDetailDao = dao,
            dispatchers = dispatchers,
            maxCacheSize = 3
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()


    @Test
    fun `observeCachedDetail emits null when cache empty`() = runTest(dispatcher) {
        every { dao.observe("octocat") } returns flowOf(null)

        repository.observeCachedDetail("octocat").test {
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCachedDetail maps entity to domain`() = runTest(dispatcher) {
        every { dao.observe("octocat") } returns flowOf(sampleEntity())

        repository.observeCachedDetail("octocat").test {
            val detail = awaitItem()!!
            assertEquals("octocat", detail.login)
            assertEquals("The Octocat", detail.name)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `fetchAndCacheDetail on success upserts and trims`() = runTest(dispatcher) {
        coEvery { api.getUserDetail("octocat") } returns sampleDto()

        val result = repository.fetchAndCacheDetail("octocat")

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { dao.upsert(any()) }
        coVerify(exactly = 1) { dao.trimTo(3) }   // maxCacheSize passed in ctor
    }

    @Test
    fun `fetchAndCacheDetail on error returns Error and does not touch DB`() =
        runTest(dispatcher) {
            val boom = RuntimeException("net down")
            coEvery { api.getUserDetail(any()) } throws boom

            val result = repository.fetchAndCacheDetail("octocat")

            assertTrue(result is Resource.Error)
            assertEquals(boom, (result as Resource.Error).throwable)
            coVerify(exactly = 0) { dao.upsert(any()) }
            coVerify(exactly = 0) { dao.trimTo(any()) }
        }


    @Test
    fun `touch calls dao with current timestamp`() = runTest(dispatcher) {
        repository.touch("octocat")

        coVerify(exactly = 1) { dao.touch("octocat", any()) }
    }


    private fun sampleDto() = UserDetailDto(
        login = "octocat",
        id = 1L,
        avatarUrl = "https://avatar/1",
        htmlUrl = "https://html/1",
        name = "The Octocat",
        company = "@github",
        blog = "https://github.blog",
        location = "SF",
        bio = "hi",
        twitterUsername = null,
        publicRepos = 8,
        publicGists = 8,
        followers = 100,
        following = 9,
        createdAt = "2011-01-25",
        updatedAt = "2021-01-01"
    )

    private fun sampleEntity() = UserDetailEntity(
        login = "octocat",
        id = 1L,
        avatarUrl = "https://avatar/1",
        htmlUrl = "https://html/1",
        name = "The Octocat",
        company = "@github",
        blog = "https://github.blog",
        location = "SF",
        bio = "hi",
        twitterUsername = null,
        publicRepos = 8,
        publicGists = 8,
        followers = 100,
        following = 9,
        createdAt = "2011-01-25",
        updatedAt = "2021-01-01",
        cachedAt = 0L,
        lastAccessedAt = 0L
    )
}