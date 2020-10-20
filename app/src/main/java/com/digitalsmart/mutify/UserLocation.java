package com.digitalsmart.mutify;

import android.location.Address;
import android.location.Location;


//class defining UserLocation object
//todo: add more data to store in this class
public class UserLocation
{
    private String name = "";
    private Location location;
    private Address address;
    private float radius;

    //todo: add time, date, and time duration

    public UserLocation(Location location)
    {
        this.location = location;
    }

    public UserLocation(String name, Location location)
    {
        this.name = name;
        this.location = location;
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
}
