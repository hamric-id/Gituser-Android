package com.hamric.feature.detailuser

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hamric.core.common.Resource
import com.hamric.domain.model.UserDetail
import com.hamric.domain.repository.UserDetailRepository
import com.hamric.domain.usecase.GetUserDetailUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class UserDetailFragmentTest {

    private val cachedFlow = MutableStateFlow<UserDetail?>(null)

    private val getUserDetail: GetUserDetailUseCase = mockk(relaxed = true)
    private val repository: UserDetailRepository = mockk(relaxed = true)

    private val testModule = module {
        single { getUserDetail }
        single { repository }
        viewModel { UserDetailViewModel(getUserDetail, repository) }
    }

    @Before
    fun setUp() {
        every { repository.observeCachedDetail(any()) } returns cachedFlow
        coEvery { repository.touch(any()) } returns Unit
        coEvery { getUserDetail(any()) } returns Resource.Success(sampleDetail())

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(testModule)
        }
    }

    @After
    fun tearDown() = stopKoin()

    private fun launch() = launchFragmentInContainer<UserDetailFragment>(
        fragmentArgs = Bundle().apply { putString("login", "octocat") },
        themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
    )

    @Test
    fun showsLoginFromArgs() {
        launch()
        Thread.sleep(400)
        onView(withId(R.id.textLogin)).check(matches(withText("@octocat")))
    }

    @Test
    fun showsNameFromApi() {
        launch()
        Thread.sleep(400)
        onView(withId(R.id.textName)).check(matches(withText("The Octocat")))
    }

    @Test
    fun showsErrorBannerWhenError() {
        coEvery { getUserDetail(any()) } returns Resource.Error(RuntimeException("boom"))
        launch()
        Thread.sleep(600)

        onView(withId(R.id.groupError))
            .check(matches(withEffectiveVisibility(VISIBLE)))
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