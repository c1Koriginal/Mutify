package com.digitalsmart.mutify;


import android.location.Address;
import android.location.Location;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

//class defining UserLocation object
//todo: add more data to store in this class
public class UserLocation
{
    private String name;
    private Location location;
    private String locality;
    private String country;
    private List<Address> addressList;
    //todo: add time, date, and time duration

    public UserLocation(Location location)
    {
        this.location = location;
    }

    public UserLocation(String name)
    {
        this.name = name;
    }

    public UserLocation(String name, Location location)
    {
        this.name = name;
        this.location = location;
    }

    //this constructor initializes the address information
    //pass in the address list obtained from Geocoder
    public UserLocation(String name, List<Address> addressList)
    {
        this.name = name;
        updateAddressInfo(addressList);
    }

    public void setAddressList(List<Address> addressList)
    {
        this.addressList = addressList;
        updateAddressInfo(addressList);
    }

    private void updateAddressInfo(List<Address> l)
    {
        this.addressList = l;
        if (addressList != null && addressList.size() > 0) {
            locality = addressList.get(0).getAddressLine(0);
            country = addressList.get(0).getCountryName();
        }
    }

    public String getName()
    {
        return this.name;
    }


    public String getLocality()
    {
        return this.locality;
    }

    public String getCountry()
    {
        return this.country;
    }
}
