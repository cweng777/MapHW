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
import androidx.lifecycle.Observer
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.weng.maphw.R
import com.weng.maphw.databinding.ActivityMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    private val viewModel by viewModel<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //create locationManager, not available before onCreate
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getLocation()
        subscribeUI()
        initMap()
    }

    private fun subscribeUI() {
        //用api回傳的GeoJson座標,繪製polygon
        viewModel.coordinates.observe(this) {
            if (this@MapsActivity::mMap.isInitialized) {
                mMap.addPolygon(
                    PolygonOptions()
                        .addAll(it))
            }
        }
    }

    private fun getLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                provider = getBestProvider()
                if (provider.isNotEmpty()) {
                    locationManager.requestLocationUpdates(
                        provider, 1000, 5f, this
                    )
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        viewModel.getCoordinates()
    }

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

            //標註使用者最新的 10 個 GPS 軌跡, 這邊的理解是畫新的10個location點軌跡,每1秒或5公尺取一次location
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
        locationManager.requestLocationUpdates(provider, 1000, 5f, this)
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }
}