package com.example.styloriderapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.styloriderapp.util.PermissionUtils
import com.example.styloriderapp.util.PermissionUtils.drawPollyLine
import com.example.styloriderapp.util.PermissionUtils.getLatLangFromAddress
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class HomeActivity : StyloBaseActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
    private var long: Double = 0.0
    private var lat: Double = 0.0
    private lateinit var mMap: GoogleMap
    private var boolCheckLoc = false
    var mPlaceType: Array<String>? = null
    var mPlaceTypeName: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
        bookBt.setOnClickListener {
            bookRide()
        }
    }

    override fun getLayoutResourceID(): Int {
        return R.layout.activity_home
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }


    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        lat = location.latitude
                        long = location.longitude
                    }
                    val latLng = LatLng(lat, long)

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))

                    if (boolCheckLoc.not()) {
//                        etSource.visibility = View.VISIBLE
//                        etDest.visibility = View.VISIBLE
//                        bookBt.visibility = View.VISIBLE

                        // Add a marker in Sydney and move the camera
                        val sydney = LatLng(lat, long)
                        mMap.addMarker(MarkerOptions().position(sydney).title("Current Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lat,
                                    long
                                ), 15.0f
                            )
                        )

                        // Few more things we can do here:
                        // For example: Update the location of user on server
                    } else {
//                        etSource.visibility = View.INVISIBLE
//                        etDest.visibility = View.INVISIBLE
//                        bookBt.visibility = View.INVISIBLE
                    }
                }

            },
            Looper.myLooper()
        )
    }

    private fun bookRide() {
        if ("Enter source" == etSource.text.toString() ||
            "Enter destination" == etDest.text.toString()
        ) {
            Toast.makeText(
                this@HomeActivity,
                "Please enter valid address", Toast.LENGTH_SHORT
            ).show()
        } else {
            var firstAddress = getLatLangFromAddress(etSource.text.toString(), this)
            var secondAddress = getLatLangFromAddress(etDest.text.toString(), this)

            var list = mutableListOf<LatLng>()
            list.add(LatLng(firstAddress!!.lat, firstAddress?.long!!))
            list.add(LatLng(secondAddress!!.lat, secondAddress?.long!!))
           boolCheckLoc =  drawPollyLine(list, mMap)
        }
    }

    private fun initUi() {
        // Array of place types
        // Array of place types
        mPlaceType = resources.getStringArray(R.array.place_type)

        // Array of place type names

        // Array of place type names
        mPlaceTypeName = resources.getStringArray(R.array.place_type_name)

        // Creating an array adapter with an array of Place types
        // to populate the spinner

        // Creating an array adapter with an array of Place types
        // to populate the spinner
        val adapter = ArrayAdapter(
            this, R.layout.support_simple_spinner_dropdown_item,
            mPlaceTypeName!!
        )

        // Getting reference to the Spinner


        // Setting adapter on Spinner to set place types

        // Setting adapter on Spinner to set place types
        spinner2.adapter = adapter

        spinner2.onItemSelectedListener = this

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Permission is not granted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        boolCheckLoc = true
//        val selectedPosition: Int = spinner2.selectedItemPosition
//        val type = mPlaceType!![selectedPosition]
//
//        val sb = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
//        sb.append("location=$lat,$long")
//        sb.append("&radius=5000")
//        sb.append("&types=$type")
//        sb.append("&sensor=true")
//        sb.append("&key=AIzaSyCmCPnLixtQf1HnLreml-3Blu29vmsmZJ0")
//
//        // Creating a new non-ui thread task to download json data
//
//        // Creating a new non-ui thread task to download json data
//        val placesTask = PlacesTask(mMap)
//
//        // Invokes the "doInBackground()" method of the class PlaceTask
//
//        // Invokes the "doInBackground()" method of the class PlaceTask
//        placesTask.execute(sb.toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
data class LatLang(val lat: Double, val long: Double)
