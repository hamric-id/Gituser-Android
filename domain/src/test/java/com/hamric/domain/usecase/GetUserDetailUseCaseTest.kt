package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.model.UserDetail
import com.hamric.domain.repository.UserDetailRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetUserDetailUseCaseTest {

    private lateinit var repository: UserDetailRepository
    private lateinit var useCase: GetUserDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUserDetailUseCase(repository)
    }

    @Test
    fun `blank login returns Error without calling repository`() = runTest {
        val result = useCase("")

        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { repository.fetchAndCacheDetail(any()) }
    }

    @Test
    fun `whitespace login returns Error without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { repository.fetchAndCacheDetail(any()) }
    }

    @Test
    fun `trims login before delegating`() = runTest {
        val detail = sampleDetail()
        coEvery { repository.fetchAndCacheDetail("octocat") } returns Resource.Success(detail)

        val result = useCase("  octocat  ")

        assertTrue(result is Resource.Success)
        assertEquals(detail, (result as Resource.Success).data)
        coVerify(exactly = 1) { repository.fetchAndCacheDetail("octocat") }
    }

    @Test
    fun `propagates repository Error`() = runTest {
        val boom = RuntimeException("net down")
        coEvery { repository.fetchAndCacheDetail(any()) } returns Resource.Error(boom)

        val result = useCase("octocat")

        assertTrue(result is Resource.Error)
        assertEquals(boom, (result as Resource.Error).throwable)
    }

    private fun sampleDetail() = UserDetail(
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
}