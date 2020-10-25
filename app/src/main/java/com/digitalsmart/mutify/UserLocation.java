package com.digitalsmart.mutify;

import android.location.Address;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;


//class defining UserLocation object
//todo: add more data to store in this class
public class UserLocation
{
    private String name = "";
    private final Location location;
    private Address address;
    private float radius = 0f;
    private final LatLng latLng;

    //todo: add time, date, and time duration

    public UserLocation(Location location)
    {
        this.location = location;
        this.latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }

    public UserLocation(String name, Location location)
    {
        this.name = name;
        this.location = location;
        this.latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void setRadius(float rad)
    {
        this.radius = rad;
    }

    public void setAddress(Address a)
    {
        this.address = a;
    }

    public String getName()
    {
        return this.name;
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
}
