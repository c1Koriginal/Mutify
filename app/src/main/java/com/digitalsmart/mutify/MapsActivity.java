package com.digitalsmart.mutify;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalsmart.mutify.util.BlurController;
import com.digitalsmart.mutify.util.HomePager;
import com.digitalsmart.mutify.util.SectionsPagerAdapter;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import no.danielzeller.blurbehindlib.BlurBehindLayout;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener
{

    private SlidingUpPanelLayout drawer;
    private BlurBehindLayout blurBackground;
    private GoogleMap map;
    private LatLng currentLatLng;
    private UserLocation markerUserLocation;
    private Location currentLocation;
    private Location markerLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;


    private PermissionManager permissionManager;
    private UserDataManager userDataManager;

    //HomePager populates a traditional ViewPager with ViewGroups (eg. ConstraintLayout) instead of Fragments
    public static HomePager homePager;
    private SpringAnimation dragSpring;
    private SpringAnimation settleSpring;


    //initialize Google Maps
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        configureCameraIdle();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        permissionManager = new PermissionManager(this);
        permissionManager.checkPermission();


        //set up blur
        drawer = findViewById(R.id.drawer);
        homePager = findViewById(R.id.home_pager);
        homePager.setAdapter(new SectionsPagerAdapter());
        blurBackground = findViewById(R.id.blur_background);
        ImageView marker = findViewById(R.id.marker_sprite);
        drawer.addPanelSlideListener(new BlurController(findViewById(R.id.background), blurBackground));


        //initialize view model
        userDataManager = new UserDataManager();
        RecyclerView locationList = findViewById(R.id.recyclerview);
        locationList.setLayoutManager(new LinearLayoutManager(this));


        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //todo: remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing the recyclerview adapter via UserDataManager
        locationList.setAdapter(userDataManager.getAdapter());

        //initialize fused location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        initializeLocationCallBack();
        createLocationRequest();
        startLocationUpdates();
        getCurrentLocation(null);




         dragSpring = new SpringAnimation(marker, DynamicAnimation.TRANSLATION_Y, -30);
         settleSpring = new SpringAnimation(marker, DynamicAnimation.TRANSLATION_Y, 0);




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

        UserLocation l = markerUserLocation;

        if (l!= null)
        {
            name.setText(l.getName());
            country.setText(l.getCountry());
            locality.setText(l.getLocality());
        }
    }

    //todo: change this, this is a dummy method to add the current marker location to the list
    public void confirmButtonClicked(View view)
    {
        //add the current marker location to the list
        userDataManager.add(markerUserLocation);
        homePager.setCurrentItem(1, true);
    }


    //todo: add a new "locate" button that calls this method to reset the user's marker location


    @Override
    protected void onPause()
    {
        super.onPause();
        blurBackground.disable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        permissionManager.checkPermission();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        permissionManager.checkPermission();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        blurBackground.disable();
    }

    //required for API26 to work
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        //do nothing
    }

    //called when user's location changes
    @Override
    public void onLocationChanged(@NonNull Location location)
    {

    }

    //called when the location service provider is disabled
    @Override
    public void onProviderDisabled(@NonNull String provider)
    {
        permissionManager.onProviderDisabled();
    }

    //called when the location service provider is enabled
    @Override
    public void onProviderEnabled(@NonNull String provider)
    {
        permissionManager.onProviderEnabled();
        getCurrentLocation(null);
    }

    //handles permission request responses
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int result = permissionManager.onRequestPermissionsResult(requestCode, grantResults);
        if (result == 0)
        {
            getCurrentLocation(null);
            return;
        }
        if (result == 1)
        {
            Toast.makeText(this.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        if (result == 2)
        {
            Toast.makeText(this.getApplicationContext(),
                    "Mutify needs to access your location in the background, please allow access location all the time.",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    //enable the app to retrieve the marker's location
    public void configureCameraIdle()
    {
        GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle()
            {
                dragSpring.skipToEnd();
                settleSpring.start();
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

        GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = new GoogleMap.OnCameraMoveStartedListener()
        {
            @Override
            public void onCameraMoveStarted(int i)
            {
                dragSpring.start();
            }
        };
        map.setOnCameraIdleListener(onCameraIdleListener);
        map.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
    }

    //call this method to manually get the user's current location
    public void getCurrentLocation(View view)
    {
        if (fusedLocationProviderClient != null)
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    currentLocation = location;
                    currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            }
        });
    }







    //these methods are required for FusedLocationProvider to work
    protected void createLocationRequest()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates()
    {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void initializeLocationCallBack()
    {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                }
            }
        };
    }


}