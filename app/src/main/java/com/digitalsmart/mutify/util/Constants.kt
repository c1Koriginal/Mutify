package com.digitalsmart.mutify.util


//constant values
//put any key values here
object Constants {

    //constants for Geocoder JobIntentService
    const val FAILURE_RESULT = 403
    private const val PACKAGE_NAME = "com.digitalsmart.mutify"
    const val ADDRESS = "$PACKAGE_NAME.ADDRESS"
    const val SUCCESS_ADDRESS = 503
    const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
    const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
    const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"

    //constants for PermissionManager
    const val COARSE_LOCATION_REQUEST_CODE = 5556
    const val FINE_LOCATION_REQUEST_CODE = 5557
    const val BACKGROUND_LOCATION_REQUEST_CODE = 5558
    const val LOCATION_REQUEST_REJECTED = 6555
    const val BACKGROUND_LOCATION_REQUEST_REJECTED = 8555
    const val REQUEST_GRANTED = 1000
}