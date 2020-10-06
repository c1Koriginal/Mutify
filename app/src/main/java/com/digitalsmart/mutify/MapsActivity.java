package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import no.danielzeller.blurbehindlib.BlurBehindLayout;

import static com.sothree.slidinguppanel.SlidingUpPanelLayout.*;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    private SlidingUpPanelLayout drawer;
    private LocationManager locationManager;
    private Location currentLocation;
    private Marker currentMarker;
    private RecyclerView locationList;
    private UserDataManager userDataManager;
    private BlurBehindLayout blurBar;
    private static final int FINE_LOCATION_CODE = 15;
    private static final int COARSE_LOCATION_CODE = 16;
    private static final int INTERNET_CODE = 17;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //check and request location permissions
        checkAndRequest(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_CODE);
        checkAndRequest(Manifest.permission.INTERNET, INTERNET_CODE);

        if(!isLocationEnabled(this))
        {
            //todo: ask the user to turn on location service in Settings
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        //setup blur for search bar
        //call disable() when the search bar is no longer visible on the screen
        //call enable() to re-enable blur
        blurBar = findViewById(R.id.searchbar);
        blurBar.setViewBehind(findViewById(R.id.map));

        drawer = findViewById(R.id.drawer);


        initializeLocationManager();

        //initialize view model
        userDataManager = new UserDataManager();
        locationList = findViewById(R.id.recyclerview);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        //todo: remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing the recyclerview adapter via UserDataManager
        locationList.setAdapter(userDataManager.getAdapter());

    }





    //todo: remove this
    public void dummyButtonClicked(View view)
    {
        //add current location to the user location list
        userDataManager.add(new UserLocation("Current Location", currentMarker));
    }

    //check permission, returns true if permission is granted
    private boolean checkPermission(String permission, int requestCode)
    {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    //request permission
    private void requestPermission(String permission, int requestCode)
    {
        ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
    }

    //check the permission passed in, and request if permission is not granted
    private void checkAndRequest(String permission, int requestCode)
    {
        if(!checkPermission(permission, requestCode))
        {
            requestPermission(permission, requestCode);
        }
    }

    //check if location service is currently enabled
    public Boolean isLocationEnabled(Context context)
    {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            return locationManager.isLocationEnabled();
        } else
        {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    //double check location permission before initializing locationManager
    private void initializeLocationManager()
    {
        //initialize locationManager to constantly listen for user's location change
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE)&&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_CODE)&&
                checkPermission(Manifest.permission.INTERNET, INTERNET_CODE))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        else
            Toast.makeText(getApplicationContext(),
                    "Mutify requires permission to access your location in order to work properly.",
                    Toast.LENGTH_LONG)
                    .show();
    }

    //LocationListener interface implementation
    //this method is repeatedly called whenever the user's location is updated
    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        //store the user's current location and convert to LatLng
        currentLocation = location;
        LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        //add marker on the map
        if (currentMarker != null)
        {
            //only update the map marker if the map is visible
            if (drawer.getPanelState() == PanelState.COLLAPSED)
            {
                //refresh existing marker
                currentMarker.remove();
                currentMarker = map.addMarker(new MarkerOptions().position(current).title("You are here."));
                //do not call moveCamera method here
            }
        }
        else {
            //initialize marker and zoom in the camera if there is no existing marker
            currentMarker = map.addMarker(new MarkerOptions().position(current).title("You are here."));
            //avoid calling moveCamera repeatedly
            //only move the camera when the app launches
            // or when displaying detailed information of certain user-saved location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
        }
    }

    //invoked when the user turns off location service while the app is running
    @Override
    public void onProviderDisabled(@NonNull String provider)
    {
        if(!isLocationEnabled(this))
        {
            Toast.makeText(getApplicationContext(),
                    "Please turn on location service for Mutify to work properly. ",
                    Toast.LENGTH_LONG)
                    .show();

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    //invoked when the user turns on location service while the app is running
    @Override
    public void onProviderEnabled(@NonNull String provider)
    {
        if(isLocationEnabled(this))
        {
            initializeLocationManager();
        }
        else
            Toast.makeText(getApplicationContext(), "Please turn on location service for Mutify to work properly. ", Toast.LENGTH_LONG).show();

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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        blurBar.disable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initializeLocationManager();
        blurBar.enable();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        initializeLocationManager();
        blurBar.enable();
    }

}