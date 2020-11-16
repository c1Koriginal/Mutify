package com.digitalsmart.mutify.model

import android.location.Address
import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.digitalsmart.mutify.util.Constants
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import java.lang.Exception


@Entity
class UserLocation
{

    //table columns

    @PrimaryKey
    var id: String

    @ColumnInfo(name = "name")
    var name = ""

    @ColumnInfo(name = "radius")
    var radius = 0f

    @ColumnInfo(name = "delay")
    var delay = 30000

    @ColumnInfo(name = "latitude")
    var latitude: Double

    @ColumnInfo(name = "longitude")
    var longitude: Double

    @ColumnInfo(name = "address_line", defaultValue = "no address info")
    var addressLine = "address info not available"

    @ColumnInfo(name = "country", defaultValue = "no country info")
    var country = "country info not available"

    @ColumnInfo(name = "postal_code", defaultValue = "unknown zip code")
    var postalCode = "unknown zip code"




    //other data

    val latLng: LatLng get() = LatLng(latitude, longitude)

    val geofence: Geofence get() =
        Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latitude, longitude, radius)
                .setLoiteringDelay(delay)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
                .build()


    @Ignore
    var address: Address? = null
        set(a)
        {
            field = a

            try { addressLine = a!!.getAddressLine(0) }
            catch (e : Exception) { }

            try { country = a!!.countryName }
            catch (e : Exception) { }

            try { postalCode = a!!.postalCode }
            catch (e : Exception) { }
        }






    //constructor
    constructor(name: String,
                radius: Float,
                delay: Int,
                latitude: Double,
                longitude: Double,
                addressLine: String,
                country: String,
                postalCode: String)
    {
        this.name = name
        this.radius = radius
        this.delay = delay
        this.latitude = latitude
        this.longitude = longitude
        this.addressLine = addressLine
        this.country = country
        this.postalCode = postalCode
        this.id = Constants.PACKAGE_NAME + this.latitude + "_" + this.longitude
    }

    //constructor
    @Ignore
    constructor(name: String,
                location: Location)
    {
        this.name = name
        latitude = location.latitude
        longitude = location.longitude
        id = Constants.PACKAGE_NAME + latitude + "_" + longitude
    }



    //Overrides
    override fun equals(other: Any?): Boolean
    {
        return if (other is UserLocation) { other.id == this.id }
        else false
    }

    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + radius.hashCode()
        result = 31 * result + delay
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + addressLine.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + postalCode.hashCode()
        return result
    }
}