package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import mumayank.com.airlocationlibrary.AirLocation;
import no.danielzeller.blurbehindlib.BlurBehindLayout;
import java.io.IOException;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener
{
    private SlidingUpPanelLayout drawer;
    private GoogleMap map;
    private LocationManager locationManager;
    private Location currentLocation;
    private Location markerLocation;
    private UserDataManager userDataManager;
    private BlurBehindLayout blurBar;
    private BlurBehindLayout blurBackground;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private LatLng current;
    private UserLocation markerUserLocation;



    private static final int FINE_LOCATION_CODE = 15;
    private static final int COARSE_LOCATION_CODE = 16;
    private static final int INTERNET_CODE = 17;

    //HomePager populates a traditional ViewPager with ViewGroups (eg. ConstraintLayout) instead of Fragments
    public static HomePager homePager;


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
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        //setup blur for search bar and background
        //call disable() on blurBar when the search bar is no longer visible on the screen
        //call enable() on blurBar to re-enable blur
        blurBar = findViewById(R.id.searchbar);
        blurBar.setViewBehind(findViewById(R.id.map));
        drawer = findViewById(R.id.drawer);
        homePager = findViewById(R.id.home_pager);
        homePager.setAdapter(new SectionsPagerAdapter());
        blurBackground = findViewById(R.id.blur_background);
        drawer.addPanelSlideListener(new BlurController(findViewById(R.id.map), blurBackground));




        initializeLocationManager();

        //initialize view model
        userDataManager = new UserDataManager();
        RecyclerView locationList = findViewById(R.id.recyclerview);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        //todo: remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing the recyclerview adapter via UserDataManager
        locationList.setAdapter(userDataManager.getAdapter());

        configureCameraIdle();
        getCurrentLocation();

    }



    //open the bottom drawer and slide to the RecyclerView page
    public void menuButtonClicked(View view)
    {
        //add the current marker location to the list
        drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        homePager.setCurrentItem(1,true);
    }

    //todo: change this after modifying activity_maps.xml
    //open the bottom drawer and slide to the edit page
    public void addButtonClicked(View view)
    {

        //test methods to display the location info of the current marker location
        drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        homePager.setCurrentItem(0,true);
        TextView name = findViewById(R.id.add_name);
        TextView country = findViewById(R.id.add_country);
        TextView locality = findViewById(R.id.add_locality);

        name.setText(markerUserLocation.getName());
        country.setText(markerUserLocation.getCountry());
        locality.setText(markerUserLocation.getLocality());
    }

    //todo: change this, this is a dummy method to add the current marker location to the list
    public void confirmButtonClicked(View view)
    {
        //add the current marker location to the list
        userDataManager.add(markerUserLocation);
        homePager.setCurrentItem(1, true);
    }

    //check permission, returns true if permission is granted
    private boolean checkPermission(String permission)
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
        if(!checkPermission(permission))
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
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)&&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)&&
                checkPermission(Manifest.permission.INTERNET))
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
            getCurrentLocation();
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
        map.setOnCameraIdleListener(onCameraIdleListener);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        blurBar.disable();
        blurBackground.disable();
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

    @Override
    protected void onStop() {
        super.onStop();
        blurBar.disable();
        blurBackground.disable();
    }

    //this method stores the location specified by the center of the camera in markerLocation
    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng latLng = map.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                markerLocation = new Location("Camera Location");
                markerLocation.setLatitude(latLng.latitude);
                markerLocation.setLongitude(latLng.longitude);
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList != null && addressList.size() > 0) {
                        markerUserLocation = new UserLocation("Marker Location", addressList);
                }



            }
        };
    }


    //call this method to manually get the user's current location
    private void getCurrentLocation() {
        AirLocation airLocation = new AirLocation(this, true, true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(@NonNull Location location) {
                currentLocation = location;
                current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
            }

            @Override
            public void onFailed(@NonNull AirLocation.LocationFailedEnum locationFailedEnum) {
                // todo: handle failure to obtain location data
            }
        });
    }
}