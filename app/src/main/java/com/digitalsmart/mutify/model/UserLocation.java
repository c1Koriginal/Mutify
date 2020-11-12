/*
package com.digitalsmart.mutify.model;

import android.location.Location;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.digitalsmart.mutify.util.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import org.jetbrains.annotations.NotNull;


@Entity
public class UserLocation
{
    //unique id for geofencing object
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


    //sqlite required constructor
    public UserLocation(@NotNull String id, String name, float radius, int delay, double latitude, double longitude)
    {
        this.name = name;
        this.radius = radius;
        this.delay = delay;
        this. latitude = latitude;
        this.longitude = longitude;
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

    public int getDelay()
    {
        return this.delay;
    }

    public float getRadius()
    {
        return this.radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
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






    //custom constructors
    public UserLocation(Location location)
    {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude;
    }

    public UserLocation(String name, Location location)
    {
        this.name = name;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude;
    }



    //other methods
    public String getAddressLine()
    {
        return "no address info";
    }

    public String getCountry()
    {
        return "no country info";
    }

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
}
 */