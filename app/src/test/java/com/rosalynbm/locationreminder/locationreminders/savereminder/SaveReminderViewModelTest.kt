package com.rosalynbm.locationreminder.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rosalynbm.locationreminder.locationreminders.data.FakeDataSource
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.dto.Result
import com.rosalynbm.locationreminder.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
//ros subjectUnderTest_actionOrInput_resultState
    //GIVEN
    //WHEN
    //THEN

    // Needed to test code with LiveData. If we do not use this, we will get the
    // RuntimeException related to Looper in Android.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private val reminder = ReminderDataItem("Reminder 1", "Hi, remind that",
        "American Airlines Arena", 25.781339, -80.187948,
        "1000"
    )

    /**
     * Before annotation (to create a setup method and remove repeated code) is used when you
     * have repeated setup code like this vm shared between.
     */
    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

   /* @Test
    fun validateEnteredData_data_returnCorrect() {
        // Given a fresh ViewModel
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext())
        // And a reminder object
        val reminder = ReminderDTO("Reminder 1", "Hi, remind that",
            "American Airlines Arena", 25.781339, -80.187948,
            "1000"
        )

        // When validating the data
        val result = validateEnteredData(reminder)

        // Then return true
    }*/

    @Test
    fun givenData_whenValidateAndSaveReminder_returnSuccess() = runBlockingTest {

        saveReminderViewModel.validateAndSaveReminder(reminder)
        val result =
            fakeDataSource.getReminder(reminder.id) as Result.Success<ReminderDTO>

        val data = result.data
        assertThat(data.id, equals("1000"))

    }

}