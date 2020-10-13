package com.digitalsmart.mutify.util

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.JobIntentService
import com.digitalsmart.mutify.MapsActivity
import com.digitalsmart.mutify.util.Constants.ADDRESS
import com.digitalsmart.mutify.util.Constants.FAILURE_RESULT
import com.digitalsmart.mutify.util.Constants.LOCATION_DATA_EXTRA
import com.digitalsmart.mutify.util.Constants.RECEIVER
import com.digitalsmart.mutify.util.Constants.RESULT_DATA_KEY
import com.digitalsmart.mutify.util.Constants.SUCCESS_ADDRESS
import java.io.IOException
import java.util.Locale

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
/**
 * This constructor is required, and calls the super IntentService(String)
 * constructor with the name for a worker thread.
 */
class FetchAddressJobIntentService:JobIntentService()
{
    private val tag = "ADDRESS_SERVICE"

    private var resultReceiver: ResultReceiver? = null

    var errorMessage = ""

    override fun onHandleWork(intent: Intent) {

        resultReceiver = intent.getParcelableExtra(RECEIVER)

        //Checks if receiver was properly registered
        //MainActivity must pass the receiver as bundle extra
        if (resultReceiver == null) {
            Log.wtf(tag, "No receiver received. There is nowhere to send the results")
            return
        }
        //Get the location passed to this service through an extra.

        val location: Location? = intent.getParcelableExtra(LOCATION_DATA_EXTRA)

        //Make sure the location is really sent
        if (location == null) {
            errorMessage = "no location data provided"
            Log.wtf(tag, errorMessage)
            deliverResultToReceiver()
            return
        }
        //Setting locale
        val geocoder = Geocoder(this, Locale.ROOT)
        //Address found

        Log.e(tag, location.toString())

        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: IOException) { //Catches network or i/o problems
            errorMessage = "service not available"
            Log.e(tag, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) { //Error in latitude or longitude data
            errorMessage = "invalid latitude or longitude"
            Log.e(
                    tag,
                    errorMessage + ". Latitude = " + location.latitude +
                            ", Longitude = " + location.longitude,
                    illegalArgumentException
            )
        }
        //Handles cases where no addresses where found
        if (addresses == null || addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found"
                Log.e(tag, errorMessage)
            }
            deliverResultToReceiver()
        } else {
            val address = addresses[0]
            deliverAddressToReceiver(address)
        }
    }

    private fun deliverAddressToReceiver(
            address: Address
    ){
        val bundle = Bundle()
        bundle.putParcelable(ADDRESS,address)
        resultReceiver?.send(SUCCESS_ADDRESS,bundle)
    }

    private fun deliverResultToReceiver() {
        val bundle = Bundle()
        bundle.putString(RESULT_DATA_KEY, errorMessage)
        resultReceiver!!.send(FAILURE_RESULT, bundle)
    }


    companion object
    {
        @JvmStatic
        fun enqueueWork(mapsActivity: MapsActivity, intent: Intent)
        {
            enqueueWork(mapsActivity,FetchAddressJobIntentService::class.java, 1, intent)
        }
    }
}
