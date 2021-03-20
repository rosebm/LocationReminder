package com.rosalynbm.locationreminder.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rosalynbm.locationreminder.locationreminders.MainCoroutineRule
import com.rosalynbm.locationreminder.locationreminders.data.FakeDataSource
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    /**
     * Essential when testing LiveData. JUnit rules are classes that allows you to run some code
     * before or after each test runs. Ensures that test results happen synchronously
     */
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var context: Application

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        stopKoin()

        fakeDataSource = FakeDataSource()
        context = ApplicationProvider.getApplicationContext()
        remindersListViewModel = RemindersListViewModel(context, fakeDataSource)
    }

    @Test
    fun `givenData whenLoadingReminder returnNotNull`() = mainCoroutineRule.runBlockingTest {
        //GIVEN a reminder
        val reminder = ReminderDTO("Reminder 1", "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1000"
        )

        fakeDataSource.saveReminder(reminder)

        val result = remindersListViewModel.loadReminders()
        assertThat(result, (not(nullValue())))
        assertThat(remindersListViewModel.remindersList.value?.size, equalTo(1))
    }

    @Test
    fun `givenNoData whenLoadingReminder returnNull`() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(),
            `is` (false))
    }

    @Test
    fun `givenData whenLoadingReminder checkLoading`() = mainCoroutineRule.runBlockingTest {
        //GIVEN a reminder
        val reminder = ReminderDTO("Reminder 1", "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1000"
        )

        fakeDataSource.saveReminder(reminder)

        // Pause dispatcher so we can see the loading status
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        var showLoading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading, `is` (true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        showLoading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading, `is` (false))
    }

    @Test
    fun `givenForceError whenLoadingReminder ShowError`() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.setForceError(true)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is` ("Reminder not found"))
    }

}