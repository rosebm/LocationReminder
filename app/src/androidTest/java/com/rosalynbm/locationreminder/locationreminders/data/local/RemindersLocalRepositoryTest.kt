package com.rosalynbm.locationreminder.locationreminders.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database : RemindersDatabase
    private lateinit var reminderDao : RemindersDao
    private lateinit var fakeRemindersLocalRepository: RemindersLocalRepository
    private var shouldReturnError = false

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // In-memory version of the database
        database = Room.inMemoryDatabaseBuilder(context,
            RemindersDatabase::class.java).allowMainThreadQueries().build()

        reminderDao = database.reminderDao()
        fakeRemindersLocalRepository = RemindersLocalRepository(reminderDao, Dispatchers.Main)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun givenReminders_WhenGettingList_ReturnSuccess() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)

        val remindersList =
            fakeRemindersLocalRepository.getReminders() as Result.Success
        assertThat(remindersList.data.size, `is`(2))
        assertThat(remindersList.data, notNullValue())
    }

    @Test
    fun givenReminders_WhenGettingList_ReturnEmpty() = runBlocking {
        // GIVEN no data
        reminderDao.deleteAllReminders()

        // WHEN getting reminders
        val remindersList =
            fakeRemindersLocalRepository.getReminders() as Result.Success

        // THEN return error
        assertThat(remindersList.data.size, `is`(0))
    }

    @Test
    fun givenReminders_WhenSaving_ReturnSuccess() = runBlocking {
        val reminder = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")

        fakeRemindersLocalRepository.saveReminder(reminder)
        val result =
            fakeRemindersLocalRepository.getReminder("1") as Result.Success

        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun givenReminders_WhenSaving_ReturnError() = runBlocking {
        val reminder = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")

        fakeRemindersLocalRepository.saveReminder(reminder)
        val result =
            fakeRemindersLocalRepository.getReminder("2") as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun givenReminders_WhenGettingReminder_ReturnSuccess() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)

        val result =
            fakeRemindersLocalRepository.getReminder("1") as Result.Success

        assertThat(result.data.id, `is`(reminder1.id))
        assertThat(result.data.title, `is`(reminder1.title))
        assertThat(result.data.description, `is`(reminder1.description))
        assertThat(result.data.location, `is`(reminder1.location))
        assertThat(result.data.latitude, `is`(reminder1.latitude))
        assertThat(result.data.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun givenReminders_WhenGettingReminder_ReturnError() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        reminderDao.saveReminder(reminder1)

        val result =
            fakeRemindersLocalRepository.getReminder("2") as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun givenReminders_WhenDeletingAll_ReturnSuccess() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)

        val resultAfterSaved =
            fakeRemindersLocalRepository.getReminders() as Result.Success

        assertThat(resultAfterSaved.data.size, `is`(2))

        fakeRemindersLocalRepository.deleteAllReminders()

        val resultAfterDeleted =
            fakeRemindersLocalRepository.getReminders() as Result.Success

        assertThat(resultAfterDeleted.data.size, `is`(0))
    }

}