package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
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



    //check and request permission
    private void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //check and request location permissions
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_CODE);
        checkPermission(Manifest.permission.INTERNET, INTERNET_CODE);



        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        //setup blur for search bar
        //call disable() when the search bar is no longer visible
        //call enable() to re-enable blur
        blurBar = findViewById(R.id.searchbar);
        blurBar.setViewBehind(findViewById(R.id.map));

        drawer = findViewById(R.id.drawer);



        //initialize locationManager to constantly listen for user's location change
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        //initialize view model
        userDataManager = new UserDataManager();
        locationList = findViewById(R.id.recyclerview);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        //todo remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing data using UserDataManager
        locationList.setAdapter(userDataManager.getAdapter());

    }


    //todo remove this
    public void dummyButtonClicked(View view)
    {
        //add current location to the user location list
        userDataManager.add(new UserLocation("Current Location", currentMarker));
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        blurBar.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blurBar.enable();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        blurBar.enable();
    }
}