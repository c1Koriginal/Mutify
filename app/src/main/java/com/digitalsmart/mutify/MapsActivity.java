package com.digitalsmart.mutify;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.digitalsmart.mutify.databinding.ActivityMapsBinding;
import com.digitalsmart.mutify.util.BlurController;
import com.digitalsmart.mutify.util.Constants;
import com.digitalsmart.mutify.util.FetchAddressJobIntentService;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener
{
    private GoogleMap map;
    private LatLng currentLatLng;
    private UserLocation markerUserLocation;
    private Location currentLocation;
    private Location markerLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private AddressResultReceiver addressResultReceiver;


    private PermissionManager permissionManager;

    //initialize view model
    private final UserDataManager userDataManager = new UserDataManager();


    private SpringAnimation dragSpring;
    private SpringAnimation settleSpring;


    //data binding object from activity_maps.xml
    //to access any View/layout from activity_maps.xml, simply call binding.'layout id'
    //for example, to access the ImageView "@+id/markerSprite", call binding.markerSprite
    //instead of using findViewById()
    //data binding library will also automatically convert view ids like "@+id/add_page" to "addPage" for easier usage in java code
    private ActivityMapsBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);

        //check permission
        permissionManager = new PermissionManager(this);
        permissionManager.checkPermission();

        //register Geocoder intent listener
        addressResultReceiver = new AddressResultReceiver(new Handler(Looper.getMainLooper()));
        initializeComponents();

        //configure RecyclerView
        binding.locationList.setLayoutManager(new LinearLayoutManager(this));


        //initialize Google Maps fragment
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //todo: remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing the recyclerview adapter via UserDataManager
        binding.locationList.setAdapter(userDataManager.getAdapter());


        //initialize fused location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //must call these methods when implementing background service
        //remove these method calls if Mutify does not need to constantly update the user's location
        initializeLocationCallBack();
        createLocationRequest();
        startLocationUpdates();

        //retrieve user's current location at app launch
        getCurrentLocation(null);
    }




    //call these methods to start constantly updating the user's location info
    //as long as the app is still running (in the foreground or minimized)
    //app will stop updating location info if the user manually closes the app
    //this is not the same as background service, implement background service separately
    //*********************************************************************************************************************
    protected void createLocationRequest()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates()
    {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    protected void initializeLocationCallBack()
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






    //button click events
    //*********************************************************************************************************************
    //call this method to manually get the user's current location
    public void getCurrentLocation(View view)
    {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    currentLocation = location;
                    currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            });
    }


    //open the bottom drawer and slide to the RecyclerView page
    public void menuButtonClicked(View view)
    {
        //add the current marker location to the list
        binding.drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        binding.homePager.setCurrentItem(1,true);
    }

    //todo: change this after modifying activity_maps.xml
    //open the bottom drawer and slide to the edit page
    public void addButtonClicked(View view)
    {
        //test methods to display the location info of the current marker location
        binding.drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        binding.homePager.setCurrentItem(0,true);

        if (markerUserLocation!= null)
        {
            binding.addName.setText(markerUserLocation.getName());
            binding.addCountry.setText(markerUserLocation.getCountry());
            binding.addLocality.setText(markerUserLocation.getAddressLine());
        }
    }

    //todo: change this, this is a dummy method to add the current marker location to the list
    public void confirmButtonClicked(View view)
    {
        //add the current marker location to the list
        userDataManager.add(markerUserLocation);
        binding.homePager.setCurrentItem(1, true);
    }








    //override methods
    //*********************************************************************************************************************
    //initialize Google Maps
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        configureCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionManager.checkPermission();
        binding.blurLayer.disable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        permissionManager.checkPermission();
        binding.blurLayer.disable();
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
        //todo: toast messages are messy here
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int result = permissionManager.onRequestPermissionsResult(requestCode, grantResults);
        if (result == Constants.REQUEST_GRANTED)
        {
            getCurrentLocation(null);
            return;
        }
        if (result == Constants.LOCATION_REQUEST_REJECTED)
        {
            Toast.makeText(this.getApplicationContext(),
                    R.string.notify_permission,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        if (result == Constants.BACKGROUND_LOCATION_REQUEST_REJECTED)
        {
            Toast.makeText(this.getApplicationContext(),
                    R.string.background_location_permission,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }





    //add other methods here
    //*********************************************************************************************************************
    //link layout components and initialize places sdk, find views by id
    private void initializeComponents()
    {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), this.getResources().getString(R.string.maps_api_key));
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(@NotNull Place place)
            {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(@NotNull Status status)
            {
                Log.i("places_error", "An error occurred: " + status);
            }
        });



        //initialize views
        binding.homePager.addView(binding.addPage, 0);
        binding.homePager.addView(binding.listPage, 1);


        //set up blur effect and transition animations
        BlurController blurController = new BlurController(binding.background, binding.blurLayer, binding.addTile, binding.menuTile, binding.homePager);
        binding.drawer.addPanelSlideListener(blurController);
        binding.homePager.addOnPageChangeListener(blurController);


        //spring animations for map marker
        dragSpring = new SpringAnimation(binding.markerSprite, DynamicAnimation.TRANSLATION_Y, -30);
        settleSpring = new SpringAnimation(binding.markerSprite, DynamicAnimation.TRANSLATION_Y, 0);
    }
    //enable the app to retrieve the marker's location
    private void configureCamera()
    {
        GoogleMap.OnCameraIdleListener onCameraIdleListener = () -> {
            dragSpring.skipToEnd();
            settleSpring.start();
            LatLng latLng = map.getCameraPosition().target;
            markerLocation = new Location("Camera Location");
            markerLocation.setLatitude(latLng.latitude);
            markerLocation.setLongitude(latLng.longitude);

            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(this, FetchAddressJobIntentService.class);
            // Pass the result receiver as an extra to the service.
            intent.putExtra(Constants.RECEIVER, addressResultReceiver);
            //pass the marker location
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, markerLocation);
            FetchAddressJobIntentService.enqueueWork(MapsActivity.this, intent);
        };
        GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = i -> dragSpring.start();
        map.setOnCameraIdleListener(onCameraIdleListener);
        map.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
    }









    private class AddressResultReceiver extends ResultReceiver
    {
        AddressResultReceiver(Handler handler)
        {
            super(handler);
        }
        //Receives data sent from FetchAddressJobIntentService and updates the UI in MainActivity.
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            if (resultCode == Constants.SUCCESS_ADDRESS)
            {
                Address a = resultData.getParcelable(Constants.ADDRESS);
                if (a != null)
                    markerUserLocation = new UserLocation("Marker Location", a);
            }
        }
    }
}