package com.example.styloriderapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.styloriderapp.util.PermissionUtils
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
import java.util.*

import com.google.android.gms.maps.model.LatLngBounds

import com.google.android.gms.maps.model.PolylineOptions

import android.widget.ArrayAdapter
import java.lang.StringBuilder
import org.json.JSONObject

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONException

import org.json.JSONArray





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

        bookBt.setOnClickListener {
            if("Enter source" == etSource.text.toString() ||
                 "Enter destination" ==   etDest.text.toString()) {
                Toast.makeText(this@HomeActivity,
                    "Please enter valid address", Toast.LENGTH_SHORT).show()

            } else {
                var firstAddress = getLatLangFromAddress(etSource.text.toString())
                var secondAddress = getLatLangFromAddress(etDest.text.toString())

                var list = mutableListOf<LatLng>()
                list.add(LatLng(firstAddress!!.lat, firstAddress?.long!!))
                list.add(LatLng(secondAddress!!.lat, secondAddress?.long!!))
                drawPollyLine(list)
            }

        }



    }



    /** A class, to download Google Places  */
    private class PlacesTask(var mMap: GoogleMap) : AsyncTask<String?, Int?, String?>() {
        var data: String? = null

        // Invoked by execute() method of this object
        override fun doInBackground(vararg params: String?): String? {
            try {
                data = downloadUrl(params[0])
            } catch (e: java.lang.Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

        @SuppressLint("LongLogTag")
        private fun downloadUrl(strUrl: String?): String? {
            var data = ""
            var iStream: InputStream? = null
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(strUrl)

                // Creating an http connection to communicate with url
                urlConnection = url.openConnection() as HttpURLConnection

                // Connecting to url
                urlConnection.connect()

                // Reading data from url
                iStream = urlConnection.getInputStream()
                val br = BufferedReader(InputStreamReader(iStream))
                val sb = StringBuffer()
                var line: String? = ""
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                data = sb.toString()
                br.close()
            } catch (e: java.lang.Exception) {
                Log.d("Exception while downloading url", e.toString())
            } finally {
                iStream?.close()
                urlConnection?.disconnect()
            }
            return data
        }

        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(result: String?) {
            val parserTask = ParserTask(mMap)

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result)
        }
    }

    /** A class to parse the Google Places in JSON format  */
    private class ParserTask(var mMap: GoogleMap) :
        AsyncTask<String?, Int?, List<HashMap<String, String>>>() {
        var jObject: JSONObject? = null

        // Invoked by execute() method of this object
        protected override fun doInBackground(vararg params: String?): List<HashMap<String, String>>? {
            var places: List<HashMap<String, String>>? = null
            val placeJsonParser = PlaceJSONParser()
            try {
                jObject = JSONObject(params[0])
                /** Getting the parsed data as a List construct  */
                places = placeJsonParser.parse(jObject!!)
            } catch (e: java.lang.Exception) {
                Log.d("Exception", e.toString())
            }
            return places
        }

        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(list: List<HashMap<String, String>>?) {

            // Clears all the existing markers
            mMap.clear()
            for (i in list!!.indices) {

                // Creating a marker
                val markerOptions = MarkerOptions()

                // Getting a place from the places list
                val hmPlace = list[i]

                // Getting latitude of the place
                val lat = hmPlace["lat"]!!.toDouble()

                // Getting longitude of the place
                val lng = hmPlace["lng"]!!.toDouble()

                // Getting name
                val name = hmPlace["place_name"]

                // Getting vicinity
                val vicinity = hmPlace["vicinity"]
                val latLng = LatLng(lat, lng)

                // Setting the position for the marker
                markerOptions.position(latLng)

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title("$name : $vicinity")

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions)
            }
        }
    }
    private fun getLatLangFromAddress(strAddress: String?): LatLang? {
        val coder = Geocoder(this, Locale.getDefault())
        val address: List<Address>?
        return try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return LatLang(77.7, 99.0)
            }
            val location: Address = address[0]
            LatLang(location.latitude, location.longitude)
        } catch (e: Exception) {
            LatLang(77.7, 99.0)
        }
    }

    override fun getLayoutResourceID(): Int {
        return R.layout.activity_home
    }
    private fun drawPollyLine(list: MutableList<LatLng>) {
        try {
            val polyOptions = PolylineOptions()
            polyOptions.color(Color.RED)
            polyOptions.width(5f)
            polyOptions.addAll(list)

            mMap.clear()
            mMap.addPolyline(polyOptions)

            val builder = LatLngBounds.Builder()
            for (latLng in list) {
                builder.include(latLng)
            }

            val bounds = builder.build()

            //BOUND_PADDING is an int to specify padding of bound.. try 100.

            //BOUND_PADDING is an int to specify padding of bound.. try 100.
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            mMap.animateCamera(cu)
            boolCheckLoc = true
        } catch (e: Exception) {
            boolCheckLoc = false
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

class PlaceJSONParser {
    /** Receives a JSONObject and returns a list  */
    fun parse(jObject: JSONObject): List<HashMap<String, String>> {
        var jPlaces: JSONArray? = null
        try {
            /** Retrieves all the elements in the 'places' array  */
            jPlaces = jObject.getJSONArray("results")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces)
    }

    private fun getPlaces(jPlaces: JSONArray?): MutableList<HashMap<String, String>> {
        val placesCount = jPlaces!!.length()
        val placesList: MutableList<HashMap<String, String>> = ArrayList()
        var place: HashMap<String, String>? = null
        /** Taking each place, parses and adds to list object  */
        for (i in 0 until placesCount) {
            try {
                /** Call getPlace with place JSON object to parse the place  */
                place = getPlace(jPlaces[i] as JSONObject)
                placesList.add(place)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return placesList
    }

    /** Parsing the Place JSON object  */
    private fun getPlace(jPlace: JSONObject): HashMap<String, String> {
        val place = HashMap<String, String>()
        var placeName = "-NA-"
        var vicinity = "-NA-"
        var latitude = ""
        var longitude = ""
        try {
            // Extracting Place name, if available
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name")
            }

            // Extracting Place Vicinity, if available
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity")
            }
            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat")
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng")
            place["place_name"] = placeName
            place["vicinity"] = vicinity
            place["lat"] = latitude
            place["lng"] = longitude
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return place
    }
}

data class LatLang(val lat: Double, val long: Double)
