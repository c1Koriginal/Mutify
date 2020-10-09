package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import org.jetbrains.annotations.NotNull;


//manages all location related operations, including permission requests
public class PermissionManager
{
    private final MapsActivity mapsActivity;
    private LocationManager locationManager;


    private static final int COARSE_LOCATION_REQUEST_CODE = 1023;
    private static final int FINE_LOCATION_REQUEST_CODE = 1214;
    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 1113;


    //constructor
    public PermissionManager(MapsActivity activity)
    {
        mapsActivity = activity;
        checkPermissionAndService();
    }

    //check permission and location service availability
    public void checkPermissionAndService()
    {
        if(!isLocationEnabled(this.mapsActivity))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.mapsActivity.startActivity(intent);
        }
    }

    //check if location service is currently enabled
    public Boolean isLocationEnabled(Context context)
    {
        if (locationManager == null)
            locationManager = (LocationManager) mapsActivity.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            return locationManager.isLocationEnabled();
        }
        else
        {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    //invoked when location provider is disabled
    public void onProviderDisabled()
    {
        Toast.makeText(mapsActivity.getApplicationContext(),
                R.string.notify_location_service,
                Toast.LENGTH_LONG)
                .show();
    }

    //invoked when location provider is enabled
    public void onProviderEnabled()
    {
        if(isLocationEnabled(mapsActivity))
        {
            checkPermissionAndService();
        }
        else
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_location_service,
                    Toast.LENGTH_LONG)
                    .show();
    }

    //listen for permission request result
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull int[] grantResults)
    {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    System.exit(0);
                }
            case COARSE_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    System.exit(0);
                }
            case BACKGROUND_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    System.exit(0);
                }
        }
    }

    //check and ask for permissions
    public void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_LONG)
                    .show();
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_LONG)
                    .show();
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, COARSE_LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_LONG)
                    .show();
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_CODE);
        }
    }
}
