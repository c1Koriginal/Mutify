package com.digitalsmart.mutify.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.digitalsmart.mutify.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.jetbrains.annotations.NotNull;

import static com.digitalsmart.mutify.util.Constants.*;


//todo: this doesn't work
//implement this to start retrieving user's location in the background (when app is not running)
//this way we can force the system to be more responsive in dealing with geofencing transitions
public class LocationWorker extends Worker
{
    private final LocationCallback locationCallback;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private NotificationManager notificationManager;

    public LocationWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback();
    }

    @NotNull
    @Override
    public Result doWork()
    {
        showNotification();
        //createLocationRequest();
        //startLocationUpdates();

        getCurrentLocation();
        return null;
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates()
    {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(12000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation()
    {
        if (fusedLocationClient != null)
        {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
        }
    }


    private void showNotification()
    {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PACKAGE_NAME)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Mutify")
                .setContentText("Mutify is running.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID_BACKGROUND, builder.setNotificationSilent().setOngoing(true).build());
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String description = "Mutify app";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(PACKAGE_NAME, PACKAGE_NAME, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
