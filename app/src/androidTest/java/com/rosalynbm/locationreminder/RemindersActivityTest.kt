package com.rosalynbm.locationreminder

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.rosalynbm.locationreminder.locationreminders.RemindersActivity
import com.rosalynbm.locationreminder.locationreminders.data.ReminderDataSource
import com.rosalynbm.locationreminder.locationreminders.data.local.LocalDB
import com.rosalynbm.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.rosalynbm.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.rosalynbm.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.rosalynbm.locationreminder.util.DataBindingIdlingResource
import com.rosalynbm.locationreminder.util.monitorActivity
import com.rosalynbm.locationreminder.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var activityRule = ActivityTestRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


//    TODO: add End to End testing to the app

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun `createNewReminder`() = runBlocking {

        // 1. Start RemindersActivity.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder item by clicking on the FAB and saving a new reminder.
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        // 3. Open the new task in a details view.
        Espresso.onView(withId(R.id.reminderTitle)).perform(replaceText("reminder 1"))
        Espresso.onView(withId(R.id.reminderDescription)).perform(replaceText("description 1"))

        // 4. Click save the reminder.
        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        // 5. Verify it was saved.
        Espresso.onView(withText("reminder 1")).check(matches(isDisplayed()))
        Espresso.onView(withText("description 1")).check(matches(isDisplayed()))

        // 6. Make sure the activity is closed.
        activityScenario.close()
    }

    @Test
    fun `createNewReminder_and_selectLocation`() = runBlocking {

        // 1. Start RemindersActivity.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder item by clicking on the FAB and saving a new reminder.
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        // 3. Fill the reminder data
        Espresso.onView(withId(R.id.reminderTitle)).
        perform(typeText("reminder 1"), closeSoftKeyboard())

        Espresso.onView(withId(R.id.reminderDescription)).
        perform(replaceText("description 1"))

        // 4. Click save the reminder.
        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        // Click to select a location
        Espresso.onView(withId(R.id.selectLocation)).perform(click())
        delay(2000)

        //Set a location in SelectLocationFragment
        Espresso.onView(withId(R.id.locationMapFragment)).perform(click())
        delay(2000)

        // Do not select the location and click save
        Espresso.onView(withId(R.id.locationSaveButton)).perform(click())
        delay(2000)

        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        // 5. Verify it was saved.
        Espresso.onView(withText("reminder 1")).check(matches(isDisplayed()))
        Espresso.onView(withText("description 1")).check(matches(isDisplayed()))
        delay(2000)
        // 6. Make sure the activity is closed.
        activityScenario.close()
    }

    @Test
    fun `EnterReminder_throwErrorTitle`() = runBlocking {
        // 1. Start RemindersActivity.
        val activityScenario =
            ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder item by clicking on the FAB and saving a new reminder.
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        // 3. Fill the reminder data
        Espresso.onView(withId(R.id.reminderTitle)).
        perform(typeText(""), closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).
        perform(replaceText("description 1"))

        // 4. Click save the reminder.
        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        // 5. Verify it was saved.
        Espresso.onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
        // Delay to see the snackbar
        delay(3000)

        // 6. Make sure the activity is closed.
        activityScenario.close()
    }

}
