package com.rosalynbm.locationreminder.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.rosalynbm.locationreminder.R
import org.koin.androidx.viewmodel.dsl.viewModel
import com.rosalynbm.locationreminder.locationreminders.RemindersActivity
import com.rosalynbm.locationreminder.locationreminders.data.ReminderDataSource
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.local.RemindersDatabase
import com.rosalynbm.locationreminder.locationreminders.data.local.FakeDataSource
import com.rosalynbm.locationreminder.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
// MediumTest marks this as test that takes medium amount of time to run
@MediumTest
class ReminderListFragmentTest: KoinTest {

    private lateinit var database: RemindersDatabase
    private lateinit var reminderDataSource: ReminderDataSource
    private val fakeDataSource: FakeDataSource by inject()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // The Rule will make sure to launch the MainActivity directly
    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(testModules))
        }
    }

    @Test
    fun givenReminderList_CheckDataDisplayed() = runBlockingTest {

        fakeDataSource.deleteAllReminders()

        // GIVEN reminder
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // WHEN - ReminderListFragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - list with reminders is displayed on the screen
        onView(withText("reminder 1")).check(matches(isDisplayed()))
        onView(withText("reminder 2")).check(matches(isDisplayed()))
    }

    @Test
    fun givenEmptyReminderList_CheckNoDataDisplayed() = runBlockingTest {
        //GIVEN - no data
        fakeDataSource.deleteAllReminders()

        // WHEN - ReminderListFragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - No data message is displayed on the screen
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun givenRemidersScreen_WhenClicked_GoToLocationReminder() {
        // GIVEN - reminders screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Add button is clicked
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - navigate to Reminder detail screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun givenMessage_DisplayErrorMessage() {

        //GIVEN - error message
        val errorMessage = "No internet connection"

        // WHEN - reminders screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            it.showErrorMessage(errorMessage)
        }

        // THEN - error message is displayed
        onView(withText(errorMessage)).
        inRoot(withDecorView(not(activityRule.activity.window.decorView))).
        check(matches(isDisplayed()))
    }

    @Test
    fun givenMessage_DisplayToastMessage() {
        //GIVEN - message
        val message = "No internet connection"

        // WHEN - reminders screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            it.showToastMessage(message)
        }

        // THEN - toast message is displayed
        onView(withText(message)).
        inRoot(withDecorView(not(activityRule.activity.window.decorView))).
        check(matches(isDisplayed()))
    }

    @Test
    fun givenMessage_DisplaySnackBar() {

        //GIVEN - message
        val message = "No internet connection"

        // WHEN - reminders screen
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            it.showSnackBar(message)
        }

        // THEN - snackbar message is displayed
        onView(withText(message)).
        inRoot(withDecorView(not(activityRule.activity.window.decorView))).
        check(matches(isDisplayed()))
    }

    private val testModules = module {
        viewModel {
            RemindersListViewModel(
                get(),
                get() as FakeDataSource
            )
        }

        single {
            database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java).allowMainThreadQueries().build()
        }

        single {
            RemindersLocalRepository(get()) as RemindersLocalRepository
        }

      /*  single {
            database.reminderDao() as ReminderDao
        }*/

        single {
            FakeDataSource()
        }
    }

}