package com.example.styloriderapp.util

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
