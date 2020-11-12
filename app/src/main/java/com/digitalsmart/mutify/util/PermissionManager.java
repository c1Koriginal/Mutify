package com.digitalsmart.mutify.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.digitalsmart.mutify.MapsActivity;
import com.digitalsmart.mutify.R;
import org.jetbrains.annotations.NotNull;

import static com.digitalsmart.mutify.util.Constants.*;


//manages all location related operations, including permission requests
public class PermissionManager
{
    private final MapsActivity mapsActivity;
    private LocationManager locationManager;
    private int requestCount = 0;
    private AlertDialog alertDialog;




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
    @SuppressWarnings("deprecation")
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

    //todo: request count >= 4 condition is never met, need fix
    //listen for permission request result
    public int onRequestPermissionsResult(int requestCode, @NonNull @NotNull int[] grantResults)
    {
        if (requestCount >= 4)
            return REQUEST_GRANTED;
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestCount++;
                    return LOCATION_REQUEST_REJECTED;
                }
            case COARSE_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestCount++;
                    return LOCATION_REQUEST_REJECTED;
                }
            case BACKGROUND_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED  && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestCount++;
                    return BACKGROUND_LOCATION_REQUEST_REJECTED;
                }
            default:
                return REQUEST_GRANTED;
        }
    }

    //check and ask for permissions
    public void checkPermission()
    {
        NotificationManager n = (NotificationManager) mapsActivity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(!n.isNotificationPolicyAccessGranted())
        {
            if (alertDialog == null || !alertDialog.isShowing())
            {
                alertDialog = new AlertDialog.Builder(mapsActivity).create();
                alertDialog.setMessage("Please allow Mutify to modify do not disturb.");
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setOnCancelListener(dialog -> {
                    if(!n.isNotificationPolicyAccessGranted())
                    {
                        mapsActivity.startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    }
                    else
                    {
                        alertDialog.cancel();
                    }
                });
            }
        }
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_SHORT)
                    .show();
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, COARSE_LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            mapsActivity.requestPermissions(new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_CODE);
        }
    }
}
