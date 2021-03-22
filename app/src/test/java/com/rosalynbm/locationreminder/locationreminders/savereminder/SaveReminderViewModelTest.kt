package com.rosalynbm.locationreminder.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rosalynbm.locationreminder.R
import com.rosalynbm.locationreminder.locationreminders.MainCoroutineRule
import com.rosalynbm.locationreminder.locationreminders.data.FakeDataSource
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.dto.Result
import com.rosalynbm.locationreminder.locationreminders.getOrAwaitValue
import com.rosalynbm.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    // Needed to test code with LiveData. If we do not use this, we will get the
    // RuntimeException related to Looper in Android.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var context: Context

    /**
     * Before annotation (to create a setup method and remove repeated code) is used when you
     * have repeated setup code like this vm shared between.
     */
    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        stopKoin()
        fakeDataSource = FakeDataSource()
        context = ApplicationProvider.getApplicationContext()

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun givenLiveData_whenCleaning_returnNull() {
        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun givenData_whenValidateAndSaveReminder_returnSuccess() = mainCoroutineRule.runBlockingTest {

        //GIVEN a reminder
        val reminder = ReminderDataItem("Reminder 1", "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1000"
        )

        // WHEN saving reminder
        saveReminderViewModel.validateAndSaveReminder(reminder)
        val result =
            fakeDataSource.getReminder(reminder.id) as Result.Success<ReminderDTO>

        // THEN return true
        assertThat(result.data.id, `is`("1000"))
        // THEN show toast message
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue() ,
            `is` (context.getString(R.string.reminder_saved)))
    }

    @Test
    fun givenNullData_whenValidateAndSaveReminder_shouldReturnErrorNoLocation() {

        // GIVEN reminder with null location
        val emptyLocationReminder = ReminderDataItem("Reminder 1", "Hi, remind that",
            null, null, null,
            "1000"
        )

        // WHEN saving reminder
        val result = saveReminderViewModel.validateEnteredData(emptyLocationReminder)

        //THEN - Show snackBar with error message Select Location
        assertThat(result, `is` (false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue() , `is` (R.string.err_select_location))
    }

    @Test
    fun givenNullData_whenValidateAndSaveReminder_shouldReturnErrorNoTitle() {

        // GIVEN reminder with null location
        val emptyTitleReminder = ReminderDataItem(null, "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1000"
        )

        // WHEN saving reminder
        val result = saveReminderViewModel.validateEnteredData(emptyTitleReminder)

        //THEN - Show snackBar with error message Enter title
        assertThat(result, `is` (false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue() , `is` (R.string.err_enter_title))
    }

    @Test
    fun `givenData WhenSavingReminder CheckLoading`() = mainCoroutineRule.runBlockingTest {
        //GIVEN a reminder
        val reminder = ReminderDataItem("Reminder 1", "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1001"
        )

        // Pause dispatcher so we can see the loading status
        mainCoroutineRule.pauseDispatcher()

        // WHEN saving reminder
        saveReminderViewModel.saveReminder(reminder)

        var showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading, `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading, `is`(false))

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue() ,
            `is` (context.getString(R.string.reminder_saved)))
    }

}