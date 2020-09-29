package com.digitalsmart.mutify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import no.danielzeller.blurbehindlib.BlurBehindLayout;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location currentLocation;
    private Marker marker;
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
        BlurBehindLayout blurLayout = findViewById(R.id.searchbar);
        blurLayout.setViewBehind(findViewById(R.id.map));


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }






    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        currentLocation = location;
        LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (marker != null)
        {
            //refresh marker
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(current).title("You are here."));
        }
        else {
            //initialize marker and zoom in the camera
            marker = mMap.addMarker(new MarkerOptions().position(current).title("You are here."));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
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
        mMap = googleMap;
    }
}