package com.hamric.feature.searchuser

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hamric.core.common.Resource
import com.hamric.domain.model.User
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class SearchFragmentTest {

    private val cachedUsers = MutableStateFlow<List<User>>(emptyList())

    private val searchUsers: SearchUsersUseCase = mockk(relaxed = true)
    private val observeCached: ObserveCachedUsersUseCase = mockk(relaxed = true)

    private val testModule = module {
        single { searchUsers }
        single { observeCached }
        viewModel { SearchViewModel(searchUsers, observeCached) }
    }

    @Before
    fun setUp() {
        every { observeCached(any()) } answers { cachedUsers }
        coEvery { searchUsers(any()) } returns Resource.Success(Unit)

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(testModule)
        }
    }

    @After
    fun tearDown() = stopKoin()

    private fun launch() = launchFragmentInContainer<SearchFragment>(
        themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
    )

    @Test
    fun searchField_isDisplayed() {
        launch()
        onView(withId(R.id.editSearch)).check(matches(isDisplayed()))
    }

    @Test
    fun typingQuery_showsUsersInRecyclerView() {
        launch()

        cachedUsers.value = listOf(
            User(1L, "octocat", "https://avatar/1", "https://html/1")
        )

        onView(withId(R.id.editSearch))
            .perform(typeText("octo"), closeSoftKeyboard())


        Thread.sleep(500) // wait LiveData/Flow to propagate

        onView(withId(R.id.recyclerUsers)).check { view, noViewFound ->
            if (noViewFound != null) throw noViewFound
            val rv = view as RecyclerView
            if (rv.adapter?.itemCount != 1) {
                throw AssertionError("Expected 1 item, got ${rv.adapter?.itemCount}")
            }
        }
    }

    @Test
    fun errorState_showsErrorGroup() {
        every { observeCached(any()) } returns flowOf(emptyList())
        coEvery { searchUsers(any()) } returns Resource.Error(RuntimeException("boom"))

        launch()

        onView(withId(R.id.editSearch))
            .perform(typeText("octo"), closeSoftKeyboard())

        Thread.sleep(800)

        onView(withId(R.id.groupError))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        every { observeCached(any()) } returns flowOf(emptyList())
        coEvery { searchUsers(any()) } returns Resource.Success(Unit)

        launch()

        onView(withId(R.id.editSearch))
            .perform(typeText("zzzz"), closeSoftKeyboard())

        Thread.sleep(800)

        onView(withId(R.id.textEmpty))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }
}