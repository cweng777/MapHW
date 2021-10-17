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
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.weng.maphw.R
import com.weng.maphw.databinding.ActivityMapsBinding
import com.weng.maphw.util.ImageUtil.getBitmapDescriptor
import com.weng.maphw.util.ImageUtil.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1111
    }

//    private lateinit var mMap: GoogleMap
    private var mMap: GoogleMap? = null
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

//        getLocation() //original I wrote here
        subscribeUI()
        initMap() //map ready first
        getLocation()  //<= 移這



        setEventListener()
    }

    private fun setEventListener() {
        //顯示Rating bar status (額外功能)
        binding.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                Toast.makeText(this, rating.toString(),Toast.LENGTH_SHORT).show();
            }

        //set map type toggle
        binding.map2CoverView.setOnClickListener {
            val map1 = mMap
            val map2 = mMap2
            if (map1 == null || map2 == null) {
                return@setOnClickListener
            } else {
                map1.mapType = if (map1.mapType == GoogleMap.MAP_TYPE_NORMAL) { GoogleMap.MAP_TYPE_SATELLITE} else {GoogleMap.MAP_TYPE_NORMAL}
                map2.mapType = if (map2.mapType == GoogleMap.MAP_TYPE_NORMAL) { GoogleMap.MAP_TYPE_SATELLITE} else {GoogleMap.MAP_TYPE_NORMAL}
            }
        }
    }

    private fun subscribeUI() {
        //用api回傳的GeoJson座標,繪製polygon
        viewModel.coordinates.observe(this) {
//            if (this@MapsActivity::mMap.isInitialized) {
//                mMap.addPolygon(
//                    PolygonOptions()
//                        .addAll(it))
//            }
            mMap?.addPolygon(
                PolygonOptions()
                    .addAll(it))
        }
    }

    private fun getLocation() { //取目前位置座標
        if (checkPermission()) {
            if (isLocationEnabled()) {
                provider = getBestProvider()
                if (provider.isNotEmpty()) { //1秒 或 5公尺 後 呼 onLocationChanged 方法
                    locationManager.requestLocationUpdates( //經測試time(1s) update 只有一次
                        provider, 1000, 5f, this //<= this is the listener object(this activity) that implements LocationListener
                    )
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show() //gps, network
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    //Make sure use this method after checking the permission, do this method because some phones use
    //Network provider, some phones use GPS provider   //not yet done, 模擬器還是不行
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
//        CoroutineScope(Main).launch {
//            if (this@MapsActivity::mMap.isInitialized) {
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
//            }
//        }
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        mMap2?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private var mMap2: GoogleMap? = null

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //experiment: 2nd map
        val mapFragment2 = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment2.getMapAsync {
            mMap2 = it
            mMap2?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
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
        viewModel.getCoordinates() //map ready 後才call api拿台北市地圖

        //環境設定。
        mMap?.uiSettings?.apply {
            //設定 右下角的放大縮小功能
            isZoomControlsEnabled = true
            //設定 左上角的指南針，要兩指旋轉才會出現
            isCompassEnabled = true
            //設定 右下角的導覽及開啟 Google Map功能
//            isMapToolbarEnabled = true
        }
        //設定衛星地圖 //todo: 改成toggle的方式 找: ex.https://stackoverflow.com/questions/7064857/making-an-android-map-menu-to-change-map-type
//        mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE

        mMap?.setOnCameraMoveListener {
            val mPosition = mMap?.cameraPosition?.target
            mPosition?.let {
                mMap2?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 17f))
            }
        }
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

    private var mMarker: Marker? = null

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location) { //callback
        Log.d("point", "(${location.latitude},${location.longitude})")
        if (isFirstTimeToThisPage) {
            mMap?.setInfoWindowAdapter(MyInfoWindowAdapter(this))
            mMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title("現在位置")
                    .snippet("這裡可以顯示相關資訊，太過長會被截掉,隨便啦")
                    .icon(getBitmapDescriptor(
                        this,
                        R.drawable.diamond_little,
                        60.px, //dp轉px
                        60.px
                    ))
            )
            mMarker?.showInfoWindow() //顯示mark上的框框

            //加一個marker
            mMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude+0.001))
                    .title("許願池")
                    .snippet("一個兩個三個,四個")
            )

            //設置marker window點擊事件
            mMap?.setOnInfoWindowClickListener {
                Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
            }

            moveCamera(LatLng(location.latitude, location.longitude), 17f)
            isFirstTimeToThisPage = false
        }
//        else { //test code: after 1 second, remove the marker
//            mMarker?.remove()
//        }

        locationList.add(LatLng(location.latitude, location.longitude))
//        if (this@MapsActivity::mMap.isInitialized) {
//            mMap.isMyLocationEnabled = true
//
//            //標註使用者最新的 10 個 GPS 軌跡, 這邊的理解是畫新的10個location點軌跡,每1秒或5公尺取一次location
//            if (locationList.size in 1..10) {
//                mMap.addPolyline(
//                    PolylineOptions()
//                        .addAll(locationList)
//                )
//            } else if (locationList.size > 10) {
//                mMap.addPolyline(
//                    PolylineOptions()
//                        .clickable(true)
//                        .addAll(locationList.takeLast(10).reversed()) //check later
//                )
//            }
//        }
        mMap?.isMyLocationEnabled = true

        //標註使用者最新的 10 個 GPS 軌跡, 這邊的理解是畫新的10個location點軌跡,每1秒或5公尺取一次location
        if (locationList.size in 1..10) {
            mMap?.addPolyline(
                PolylineOptions()
                    .addAll(locationList)
            )
        } else if (locationList.size > 10) {
            mMap?.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .addAll(locationList.takeLast(10).reversed()) //check later
            )
        }

        locationManager.requestLocationUpdates(provider, 1000, 5f, this)
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this) //營幕變黑就不收集位置了
    }


}