package com.rosalynbm.locationreminder.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.rosalynbm.locationreminder.BuildConfig
import com.rosalynbm.locationreminder.R
import com.rosalynbm.locationreminder.base.BaseFragment
import com.rosalynbm.locationreminder.databinding.FragmentSelectLocationBinding
import com.rosalynbm.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.rosalynbm.locationreminder.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class SelectLocationFragment: BaseFragment(), OnMapReadyCallback, View.OnClickListener {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedLocation: LatLng? = null
    private val TAG = SelectLocationFragment::class.java.simpleName

    companion object {
        private const val PERMISSION_ID = 19
        private const val ZOOM_LEVEL = 15f
        private val MIAMI_LATLNG = LatLng(25.803760, -80.130895)
        private const val REQUEST_LOCATION_PERMISSION = 1
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        initSupportMapFragment()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.locationSaveButton.setOnClickListener(this)

        return binding.root
    }

    private fun initSupportMapFragment() {
        val mapFragment =
                childFragmentManager.findFragmentById(R.id.locationMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        NavHostFragment.findNavController(this).popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
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
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Changes the map type based on the user's selection.
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            enableMyLocation()
        }

        setMapStyle(map)
        setOnMapPoiClickListener()
        //setOnMapLongClickListener()
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        var latLng: LatLng

        if (isPermissionGranted()) {
            map?.isMyLocationEnabled = true

            // Subscribe to location changes
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    location?.let {
                        latLng = LatLng(location.latitude, location.longitude)

                        map?.let {
                            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
                            it.addMarker(MarkerOptions().position(latLng).title("Marker in user's last location"))
                        }
                        Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_LONG).show()
                    }
                }

            getLastLocation()
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun setOnMapPoiClickListener() {
        map?.setOnPoiClickListener { poi ->
            //displays additional info
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                poi.latLng.latitude,
                poi.latLng.longitude
            )
            map?.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            _viewModel.selectedPOI.value = poi
            _viewModel.reminderSelectedLocationStr.value = poi.name
            _viewModel.latitude.value = poi.latLng.latitude
            _viewModel.longitude.value = poi.latLng.longitude
            locationSaveButton.visibility = View.VISIBLE
        }
    }

    private fun setOnMapLongClickListener() {
        map?.setOnMapLongClickListener { latLng ->
            //displays additional info
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
            map?.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .title(requireContext().resources.getString(R.string.dropped_pin))
                            .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            locationSaveButton.visibility = View.VISIBLE
        }
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissionsAndRetrieveLocation()
            }
        }
    }

    private fun isForegroundPermissionApproved(): Boolean {
        if (ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            return true
        }

        return false
    }

    /**
     * Method checks if foreground permissions approved.
     */
    @SuppressLint("MissingPermission")
    private fun checkPermissionsAndRetrieveLocation() {
        val provideRationale = isForegroundPermissionApproved()
        var latLng: LatLng

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            requestPermissions()
        } else {
            // Subscribe to location changes
            fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        location?.let {
                            latLng = LatLng(location.latitude, location.longitude)

                            map?.let {
                                it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
                                it.addMarker(MarkerOptions().position(latLng).title("Marker in user's last location"))
                            }
                            Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_LONG).show()
                        }
                    }
        }

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
    }

    private fun isLocationEnabled(): Boolean {
        var enabled = false
        val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        locationManager?.let {
            enabled = it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        return enabled
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.locationSaveButton -> onLocationSelected()
        }
    }

    private fun setMapStyle(map: GoogleMap?) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            ) ?: false

            if (!success)
                Timber.e("Style parsing failed.")

        } catch (ex: Resources.NotFoundException) {
            Timber.e("Error styling the map: ${ex.localizedMessage}")
        }
    }

}
