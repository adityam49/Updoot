package com.ducktapedapps.updoot.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ducktapedapps.updoot.utils.Constants
import org.hamcrest.Matchers.`is`
import org.hamcrest.collection.IsIn
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityVMTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ActivityVM
    private val fakeRedditClient = FakeRedditClient()

    @Before
    fun setupViewModel() {
        vm = ActivityVM(fakeRedditClient)
    }

    @Test
    fun `changeAccountTo Anonymous showOnlyNonLoggedInDestinations`() {
        vm.setCurrentAccount(Constants.ANON_USER)

        val actualDestinations = vm.navigationEntries.getOrAwaitValue()
        val expectedDestinations = (fakeRedditClient.nonLoggedInDestinations)
        assertThat(actualDestinations.size, `is`(expectedDestinations.size))
        actualDestinations.forEach {
            assertThat(it, IsIn(expectedDestinations))
        }
    }

    @Test
    fun `changeAccountTo UserAccount showAllDestinations`() {
        vm.setCurrentAccount(fakeRedditClient.loggedInUserName)
        val actualDestinations = vm.navigationEntries.getOrAwaitValue()
        val expectedDestinations = (fakeRedditClient.loggedInDestinations + fakeRedditClient.nonLoggedInDestinations)
        assertThat(actualDestinations.size, `is`(expectedDestinations.size))
        actualDestinations.forEach {
            assertThat(it, IsIn(expectedDestinations))
        }
    }
}