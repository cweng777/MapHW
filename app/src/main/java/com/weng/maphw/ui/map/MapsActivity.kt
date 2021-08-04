package com.weng.maphw.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.weng.maphw.R
import com.weng.maphw.databinding.ActivityMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1111
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var provider: String
    private val locationList = ArrayList<LatLng>()
    private var isFirstTimeToThisPage = true

//-----------------------play service location-----------------------------------------

//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

//    private val locationCallback = object : LocationCallback() {
//        @SuppressLint("MissingPermission")
//        override fun onLocationResult(locationResult: LocationResult) {
//           val location = locationResult.lastLocation
//            Log.d("point", "(${location.latitude},${location.longitude})")
//            moveCamera(LatLng(location.latitude, location.longitude), 14f)
//            if (this@MapsActivity::mMap.isInitialized) {
//                mMap.isMyLocationEnabled = true
//            }
//        }
//    }
//-------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getLocation()
    }

    private fun getLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {

                //-----------------------play service location-----------------------------------------
//                val locationRequest = LocationRequest.create().apply {
//                    interval = 1000
//                    fastestInterval = 1000
//                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                    maxWaitTime= 1000
//                }
//
//                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//                fusedLocationProviderClient.requestLocationUpdates(
//                    locationRequest,
//                    locationCallback,
//                    Looper.getMainLooper()
//                )
//                initMap()
                //-------------------------------------------------------------------------------------

                provider = getBestProvider()

                if (provider.isNotEmpty()) {
                    locationManager.requestLocationUpdates(
                        provider, 1000, 5f, this
                    )


//                    locationManager.requestLocationUpdates(
//                        provider, 500, 5f
//                    ) { location ->
//                        Log.d("point", "(${location.latitude},${location.longitude})")
//                        moveCamera(LatLng(location.latitude, location.longitude), 14f)
//                        if (this@MapsActivity::mMap.isInitialized) {
//                            mMap.isMyLocationEnabled = true
//                        }
//                    }
                    initMap()
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    //Make sure use this method after checking the permission, do this method because some phones use
    //Network provider, some phones use GPS provider
    @SuppressLint("MissingPermission")
    private fun getBestProvider(): String {
        val providers: List<String> = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l: Location = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        val provider = bestLocation?.provider?:""
        Log.d("isLocationEnabled", "best provider: ${provider.toString()}")
        return provider
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        CoroutineScope(Main).launch {
            if (this@MapsActivity::mMap.isInitialized) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
            }
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                getLocation()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        // Add polylines to the map. Test only
//        // Polylines are useful to show a route or some other connection between points.
//        val polyline1 = googleMap.addPolyline(
//            PolylineOptions()
//            .clickable(true)
//            .add(
//                LatLng(25.007293, 121.516600),
//                LatLng(25.009971, 121.518032),
//                LatLng(25.008970, 121.520661)
//            )
//        )
//        // and set the zoom factor so most of Australia shows on the screen.
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.006253, 121.519915), 14f))
    }

    override fun onPause() {
        super.onPause()
        //-----------------------play service location-----------------------------------------
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        //-------------------------------------------------------------------------------------

        locationManager.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location) {
        Log.d("point", "(${location.latitude},${location.longitude})")
        if (isFirstTimeToThisPage) {
            moveCamera(LatLng(location.latitude, location.longitude), 17f)
            isFirstTimeToThisPage = false
        }
        locationList.add(LatLng(location.latitude, location.longitude))
        if (this@MapsActivity::mMap.isInitialized) {
            mMap.isMyLocationEnabled = true

            if (locationList.size in 1..10) {
                mMap.addPolyline(
                    PolylineOptions()
                        .addAll(locationList)
                )
            } else if (locationList.size > 10) {
                mMap.addPolyline(
                    PolylineOptions()
                        .clickable(true)
                        .addAll(locationList.takeLast(10).reversed())
                )
            }
        }

        //test only
//       mMap.addPolyline(
//            PolylineOptions()
//            .add(
//                LatLng(25.007293, 121.516600),
//                LatLng(25.009971, 121.518032),
//                LatLng(25.008970, 121.520661)
//            )
//        )

        locationManager.requestLocationUpdates(provider, 1000, 5f, this)
    }
}