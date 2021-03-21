package com.rosalynbm.locationreminder.locationreminders.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var reminderDao : RemindersDao
    private lateinit var database : RemindersDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // In-memory version of the database
        database = Room.inMemoryDatabaseBuilder(context,
                RemindersDatabase::class.java).allowMainThreadQueries().build()

        reminderDao = database.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun getReminders() = runBlocking {
        val reminder = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        reminderDao.saveReminder(reminder)

        val reminderList = reminderDao.getReminders()
        assertThat(reminderList, notNullValue())
        assertThat(reminderList.size, `is`(1))
    }

    @Test
    fun saveReminder() = runBlocking {
        val reminder = ReminderDTO("reminder 1", "description 1",
        "American Airlines Arena", 25.781374, -80.187917, "1")
        reminderDao.saveReminder(reminder)

        val reminderSaved = reminderDao.getReminderById(reminder.id)
        assertThat(reminderSaved, notNullValue())
        assertThat(reminderSaved?.id, `is`(reminderSaved?.id))
        assertThat(reminderSaved?.title, `is`(reminderSaved?.title))
        assertThat(reminderSaved?.location, `is`(reminderSaved?.location))
    }

    @Test
    fun getRemindersById() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)

        val reminder = reminderDao.getReminderById("2")
        assertThat(reminder?.id, `is`(reminder2.id))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        val reminder1 = ReminderDTO("reminder 1", "description 1",
            "American Airlines Arena", 25.781374, -80.187917, "1")
        val reminder2 = ReminderDTO("reminder 2", "description 2",
            "American Airlines Arena", 25.781374, -80.187917, "2")
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)

        reminderDao.deleteAllReminders()
        val deletedList = reminderDao.getReminders()
        assertThat(deletedList.size, `is`(0))

    }

}