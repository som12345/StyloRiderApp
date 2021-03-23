package com.example.styloriderapp.util

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.HashMap

/** A class to parse the Google Places in JSON format  */
 class ParserTask(var mMap: GoogleMap) :
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