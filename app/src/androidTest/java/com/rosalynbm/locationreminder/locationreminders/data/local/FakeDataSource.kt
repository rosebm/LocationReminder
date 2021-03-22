package com.rosalynbm.locationreminder.locationreminders.data.local

import com.rosalynbm.locationreminder.locationreminders.data.ReminderDataSource
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.dto.Result
import java.util.ArrayList

/**
 * FakeDataSource acts as a test double to the LocalDataSource. Replaces the Data Layer
 * to test the app in isolation.
 */
class FakeDataSource : ReminderDataSource {

    private var returnError = false

    private val reminder1 = ReminderDTO("reminder 1", "description 1",
        "American Airlines Arena", 25.781374, -80.187917, "1")
    private val reminder2 = ReminderDTO("reminder 2", "description 2",
        "American Airlines Arena", 25.781374, -80.187917, "2")
    private val reminder3 = ReminderDTO("reminder 3", "description 3",
        "American Airlines Arena", 25.781374, -80.187917, "3")
    private val reminders: MutableList<ReminderDTO> = mutableListOf(reminder1, reminder2, reminder3)

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (returnError)
            return Result.Error("Reminder not found", 404)

        reminders.let { return Result.Success(ArrayList(it)) }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find { it.id == id }

        return if (reminder != null)
            Result.Success(reminder)
        else
            Result.Error("Reminder not found", 404)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    fun setForceError(value: Boolean) {
        returnError = value
    }


}