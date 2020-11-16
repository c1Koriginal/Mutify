package com.digitalsmart.mutify.model;

import android.location.Address;
import android.location.Location;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.digitalsmart.mutify.util.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import org.jetbrains.annotations.NotNull;


@Entity
public class UserLocationOld
{
    //table columns

    @PrimaryKey
    @NotNull
    private String id;

    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "radius")
    private float radius = 0f;

    @ColumnInfo(name = "delay")
    private int delay = 30000;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "address_line", defaultValue = "no address info")
    private String addressLine = "address info not available";

    @ColumnInfo(name = "country", defaultValue = "no country info")
    private String country = "country info not available";

    @ColumnInfo(name = "postal_code", defaultValue = "unknown zip code")
    private String postalCode = "unknown zip code";

    //sqlite required constructor
    public UserLocationOld(String name,
                           float radius,
                           int delay,
                           double latitude,
                           double longitude,
                           String addressLine,
                           String country,
                           String postalCode)
    {
        this.name = name;
        this.radius = radius;
        this.delay = delay;
        this. latitude = latitude;
        this.longitude = longitude;
        this.addressLine = addressLine;
        this.country = country;
        this.postalCode = postalCode;
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude;
    }


    //getters and setters
    public void setId(@NotNull String id)
    {
        this.id = id;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void setName(String newName)
    {
        this.name = newName;
    }

    public void setRadius(float rad)
    {
        this.radius = rad;
    }

    public void setDelay(int delay)
    {
        this.delay = delay;
    }

    public void setAddressLine(String addressLine)
    {
        this.addressLine = addressLine;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    public int getDelay()
    {
        return this.delay;
    }

    public float getRadius()
    {
        return this.radius;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    @NotNull
    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public String getAddressLine()
    {
        return addressLine;
    }

    public String getCountry()
    {
        return country;
    }

    public String getPostalCode()
    {
        return postalCode;
    }








    //constructors
    @Ignore
    public UserLocationOld(Location location)
    {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude;
    }

    @Ignore
    public UserLocationOld(String name, Location location)
    {
        this.name = name;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude;
    }







    //other methods
    public LatLng getLatLng()
    {
        return new LatLng(latitude, longitude);
    }


    public Geofence getGeofence()
    {
        return new Geofence.Builder()
                .setRequestId(this.id)
                .setCircularRegion(this.latitude, this.longitude, radius)
                .setLoiteringDelay(this.delay)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
    }


    public void setAddress(Address a)
    {
        try { addressLine = a.getAddressLine(0); }
        catch (Exception ignored) { }

        try { country = a.getCountryName(); }
        catch (Exception ignored) { }

        try { postalCode = a.getPostalCode(); }
        catch (Exception ignored) { }
    }



    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj)
    {
        if (obj instanceof UserLocationOld)
            return this.id.equals(((UserLocationOld) obj).getId());
        else
            return false;
    }
}
