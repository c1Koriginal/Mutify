package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import mumayank.com.airlocationlibrary.AirLocation;
import java.io.IOException;
import java.util.List;




//todo: research on FusedLocation ***
public class LocationServiceManager implements OnMapReadyCallback, LocationListener
{

    private static final int FINE_LOCATION_CODE = 15;
    private static final int COARSE_LOCATION_CODE = 16;
    private static final int INTERNET_CODE = 17;


    private final MapsActivity mapsActivity;
    private GoogleMap map;
    private LocationManager locationManager;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private LatLng current;
    private UserLocation markerUserLocation;
    private Location currentLocation;
    private Location markerLocation;

    public LocationServiceManager(MapsActivity activity)
    {
        this.mapsActivity = activity;
        checkAndRequest(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.INTERNET, INTERNET_CODE);

        if(!isLocationEnabled(this.mapsActivity))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.mapsActivity.startActivity(intent);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) mapsActivity.getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    //check permission and location service availability
    public void checkPermissionAndService()
    {
        checkAndRequest(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.INTERNET, INTERNET_CODE);

        if(!isLocationEnabled(this.mapsActivity))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.mapsActivity.startActivity(intent);
        }
    }

    //return the UserLocation object generated by the marker's position on the map
    public UserLocation getMarkerUserLocation()
    {
        return this.markerUserLocation;
    }

    //return the marker's position
    public Location getMarkerLocation()
    {
        return this.markerLocation;
    }

    //check permission, returns true if permission is granted
    private boolean checkPermission(String permission)
    {
        return ContextCompat.checkSelfPermission(mapsActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    //request permission
    private void requestPermission(String permission, int requestCode)
    {
        ActivityCompat.requestPermissions(mapsActivity, new String[] { permission }, requestCode);
    }

    //check the permission passed in, and request if permission is not granted
    private void checkAndRequest(String permission, int requestCode)
    {
        if(!checkPermission(permission))
        {
            requestPermission(permission, requestCode);
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


    //LocationListener interface implementation
    //this method is repeatedly called whenever the user's current location is updated
    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        //do nothing
        //add any code here to handle user's current location from automatic update
    }

    //invoked when the user turns off location service while the app is running
    @Override
    public void onProviderDisabled(@NonNull String provider)
    {
        if(!isLocationEnabled(mapsActivity))
        {
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_location_service,
                    Toast.LENGTH_LONG)
                    .show();

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mapsActivity.startActivity(intent);
        }
    }

    //invoked when the user turns on location service while the app is running
    @Override
    public void onProviderEnabled(@NonNull String provider)
    {
        if(isLocationEnabled(mapsActivity))
        {
            checkPermissionAndService();
            getCurrentLocation();
        }
        else
            Toast.makeText(mapsActivity.getApplicationContext(),
                    R.string.notify_location_service,
                    Toast.LENGTH_LONG)
                    .show();
    }

    //no need to add code here, but this method has to be overridden in order to support Android 8.0
    //otherwise it will cause runtime exception
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        //do nothing
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setOnCameraIdleListener(onCameraIdleListener);
    }

    //retrieve the camera idle position and store in userMarkerLocation
    public void configureCameraIdle()
    {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener()
        {
            @Override
            public void onCameraIdle() {
                LatLng latLng = map.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(mapsActivity);
                markerLocation = new Location("Camera Location");
                markerLocation.setLatitude(latLng.latitude);
                markerLocation.setLongitude(latLng.longitude);
                List<Address> addressList = null;
                try
                {
                    addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (addressList != null && addressList.size() > 0)
                {
                    markerUserLocation = new UserLocation("Marker Location", addressList);
                }
            }
        };

        map.setOnCameraIdleListener(onCameraIdleListener);
    }

    //call this method to manually get the user's current location
    public void getCurrentLocation()
    {
        AirLocation airLocation = new AirLocation(mapsActivity, true, true, new AirLocation.Callbacks()
        {
            @Override
            public void onSuccess(@NonNull Location location)
            {
                currentLocation = location;
                current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
            }
            @Override
            public void onFailed(@NonNull AirLocation.LocationFailedEnum locationFailedEnum)
            {
                Toast.makeText(mapsActivity.getApplicationContext(),
                        "Failed to retrieve location information.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
