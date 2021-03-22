package com.rosalynbm.locationreminder.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.rosalynbm.locationreminder.BuildConfig
import com.rosalynbm.locationreminder.R
import com.rosalynbm.locationreminder.base.BaseFragment
import com.rosalynbm.locationreminder.base.NavigationCommand
import com.rosalynbm.locationreminder.databinding.FragmentSaveReminderBinding
import com.rosalynbm.locationreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.rosalynbm.locationreminder.locationreminders.geofence.GeofenceRepository
import com.rosalynbm.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.rosalynbm.locationreminder.utils.GeofenceUtils
import com.rosalynbm.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    companion object {
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 11
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 19
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 0
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 20
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        //intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    /**
     * Checks if the user has chosen to accept the permissions. If not, it will ask again
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            requestForegroundAndBackgroundLocationPermissions()
        }

        // main entry point for interacting with the geofencing APIs
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    private fun createReminder(): ReminderDataItem {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        return ReminderDataItem(title, description, location, latitude, longitude)
    }

    /**
     * Permission is denied if:
     * - The grantResults array is empty
     * - The grantResults array’s value at the LOCATION_PERMISSION_INDEX has a PERMISSION_DENIED
     * - The request code equals REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE and the
     * BACKGROUND_LOCATION_PERMISSION_INDEX is denied.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            // Display a snackbar explaining that the user needs location permissions in order to
            // trigger the reminders
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else
            checkDeviceLocationSettingsAndStartGeofence()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /**
     * Requests ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION (if Android 10+)
     */
    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            createReminder().let { reminder ->
                _viewModel.validateAndSaveReminder(reminder)
                buildGeofence(reminder)?.let {
                    addGeofence(it)
                }
            }
        }

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissionsArray,
            resultCode
        )
    }

    private fun checkDeviceLocationSettingsAndStartGeofence() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        // To only check if the location services is turned off
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // To prompt the user to turn on device location.
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.e("Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                // Alerts the user that location needs to be enabled so the reminders can be used
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                createReminder().let { it1 ->
                    _viewModel.validateAndSaveReminder(it1)
                }
            }
        }
    }

    private fun buildGeofence(reminderDataItem: ReminderDataItem): Geofence? {

        return reminderDataItem.latitude?.let {latitude ->
            reminderDataItem.longitude?.let { longitude ->
                Geofence.Builder()
                    .setRequestId(reminderDataItem.id)
                    .setCircularRegion(
                        latitude,
                        longitude,
                        GeofenceUtils.GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE) // will exist until the user removes it.
                    .build()
            }
        }
    }

    private fun addGeofence(geofence: Geofence) {
        val geofencingRequest = GeofenceRepository.buildGeofenceRequest(geofence)

        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    /*Toast.makeText(requireActivity(), "geofences_added",
                        Toast.LENGTH_SHORT)
                        .show()*/
                    Timber.e("Geofence added id = ${geofence.requestId}")
                }
                .addOnFailureListener {
                    /* Toast.makeText(requireContext(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()*/
                    if ((it.message != null)) {
                        Timber.w("Geofence failure ${it.message}")
                    }
                    //failure(GeofenceErrorMessages.getErrorString(context, it))
                }
        }

    }

    /**
     * Removes the geofence that don't need anymore, after consumed. Best practice to save battery
     * and CPU cycles to stop monitoring.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Timber.d(getString(R.string.geofences_removed))
                Toast.makeText(requireContext(), R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                Timber.d(getString(R.string.geofences_not_removed))
                //failure(GeofenceErrorMessages.getErrorString(context, it))
            }
        }
    }

}
