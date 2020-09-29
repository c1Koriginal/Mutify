package com.digitalsmart.mutify;


import com.google.android.gms.maps.model.Marker;

//class defining UserLocation object
//todo add more data to store in this class
public class UserLocation
{
    private String name;
    private Marker marker;
    //todo add time, date, and time duration

    public UserLocation(Marker marker)
    {
        this.marker = marker;
    }

    public UserLocation(String name)
    {
        this.name = name;
    }

    public UserLocation(String name, Marker marker)
    {
        this.name = name;
        this.marker = marker;
    }

    public String getName()
    {
        return this.name;
    }

    //todo remove this method
    public String getLocation()
    {
        if (marker != null)
            return marker.getPosition().toString();
        else
            return "no location info";
    }
}
