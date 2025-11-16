package com.example.Homework7

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.GeoApiContext
import com.google.maps.DirectionsApi
import com.google.maps.model.TravelMode
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var geoApiContext: GeoApiContext

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == 0) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                loadMap()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadMap()

        val apiKey = packageManager.getApplicationInfo(
            packageName, PackageManager.GET_META_DATA
        ).metaData.getString("com.google.android.geo.API_KEY")

        if (!apiKey.isNullOrEmpty()) {
            geoApiContext = GeoApiContext.Builder()
                .apiKey(apiKey)
                .build()
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val isAccessFineLocationGranted =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isAccessCoarseLocationGranted =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (isAccessFineLocationGranted && isAccessCoarseLocationGranted) {
            map.isMyLocationEnabled = true

            val taipei101 = LatLng(25.033611, 121.565000)
            val taipeiMainStation = LatLng(25.047924, 121.517081)

            map.addMarker(MarkerOptions().position(taipei101).title("台北101").draggable(true))
            map.addMarker(MarkerOptions().position(taipeiMainStation).title("台北車站").draggable(true))

            val polylineOpt = PolylineOptions()
                .add(taipei101)
                .add(LatLng(25.032435, 121.534905))
                .add(taipeiMainStation)
                .color(Color.BLUE)
                .width(10f)
            map.addPolyline(polylineOpt)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.035, 121.54), 13f))

            findViewById<Button>(R.id.btnWalk).setOnClickListener {
                drawBestRoute(taipei101, taipeiMainStation, TravelMode.WALKING)
            }
            findViewById<Button>(R.id.btnDrive).setOnClickListener {
                drawBestRoute(taipei101, taipeiMainStation, TravelMode.DRIVING)
            }
            findViewById<Button>(R.id.btnBike).setOnClickListener {
                drawBestRoute(taipei101, taipeiMainStation, TravelMode.BICYCLING)
            }

        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0
            )
        }
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun drawBestRoute(origin: LatLng, destination: LatLng, mode: TravelMode) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .mode(mode)
                    .await()

                CoroutineScope(Dispatchers.Main).launch {
                    if (directionsResult.routes.isNotEmpty()) {
                        val route = directionsResult.routes[0]
                        val decodedPath = PolyUtil.decode(route.overviewPolyline.encodedPath)

                        map.clear()
                        map.addMarker(MarkerOptions().position(origin).title("台北101"))
                        map.addMarker(MarkerOptions().position(destination).title("台北車站"))

                        val polylineOptions = PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.RED)
                            .width(15f)

                        map.addPolyline(polylineOptions)

                        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                        decodedPath.forEach { bounds.include(it) }
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
