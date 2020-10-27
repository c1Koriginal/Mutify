package com.digitalsmart.mutify;

import android.location.Address;
import android.location.Location;
import com.digitalsmart.mutify.util.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;



//todo: store this object in SQLite
public class UserLocation
{



    //unique id for geofencing object
    private String id = "";

    private String name = "";

    private final Location location;
    private Address address;

    private float radius = 0f;

    private final LatLng latLng;

    private int delay = 30000;

    //todo: add time, date, and time duration





    public UserLocation(Location location)
    {
        this.location = location;
        this.latLng = new LatLng(location.getLatitude(), location.getLongitude());
        this.id = Constants.PACKAGE_NAME + this.latLng.toString();
    }

    public UserLocation(String name, Location location)
    {
        this.name = name;
        this.location = location;
        this.latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public Location getLocation()
    {
        return this.location;
    }

    public void setName(String newName)
    {
        this.name = newName;
    }

    public void setRadius(float rad)
    {
        this.radius = rad;
    }

    public void setAddress(Address a)
    {
        this.address = a;
    }

    public void setDelay(int delay)
    {
        this.delay = delay;
    }

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public int getDelay()
    {
        return this.delay;
    }

    public String getAddressLine()
    {
        if (address != null)
            return this.address.getAddressLine(0);
        return "no address info";
    }

    public String getCountry()
    {
        if (address != null)
            return this.address.getCountryName();
        return "no country info";
    }

    public LatLng getLatLng()
    {
        return this.latLng;
    }

    public float getRadius()
    {
        return this.radius;
    }

    public Geofence getGeofence()
    {
        return new Geofence.Builder()
                .setRequestId(this.id)
                .setCircularRegion(this.latLng.latitude, this.latLng.longitude, radius)
                .setLoiteringDelay(this.delay)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
    }
}
