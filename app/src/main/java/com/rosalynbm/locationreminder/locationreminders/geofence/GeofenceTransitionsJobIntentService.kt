package com.rosalynbm.locationreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.rosalynbm.locationreminder.locationreminders.data.dto.ReminderDTO
import com.rosalynbm.locationreminder.locationreminders.data.dto.Result
import com.rosalynbm.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.rosalynbm.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.rosalynbm.locationreminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private lateinit var context: Context
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    val reminderLocalRepository: RemindersLocalRepository by inject()

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {

            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification

        //if (intent.action == ACTION_GEOFENCE_EVENT) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(context, geofencingEvent.errorCode)
            Timber.e("GeofenceBroadcastReceiver error: $errorMessage ")
            return
        }
        handleEvent(geofencingEvent)
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = ""

        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                Timber.d("ROS sendNotification ----->")
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

    private fun handleEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
           /* val reminder = getFirstReminder(event.triggeringGeofences)
            val message = reminder?.description
            val lat = reminder?.latitude
            val lng = reminder?.longitude

            reminder?.let{
                sendNotification(context, reminder)
            }*/

            sendNotification(event.triggeringGeofences)
        }
    }

    /**
     * If the user creates overlapping geofences, there may be multiple triggering events,
     * so, here, we pick only the first reminder object.
     */
    /*private fun getFirstReminder(triggeringGeofences: List<Geofence>): ReminderDataItem? {
        val firstGeofence = triggeringGeofences[0]

        return reminderLocalRepository.getReminder(firstGeofence.requestId)
    }*/

}