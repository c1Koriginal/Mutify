package com.digitalsmart.mutify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.digitalsmart.mutify.util.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

import static com.digitalsmart.mutify.util.Constants.PACKAGE_NAME;


//todo: add methods here to show notification
public class GeofenceBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "broadcast";

    @Override
    public void onReceive(Context context, Intent intent)
    {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();



        // Get the geo fences that were triggered. A single event can trigger
        // multiple geo fences.
        List<Geofence> triggeringFences = geofencingEvent.getTriggeringGeofences();
        int count = triggeringFences.size();
        String message;


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {
            message = " geo fences, dwelling detected";
            Log.d(TAG, count + message);
            Toast.makeText(context,count + message
                    ,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            message = " geo fences, entering detected";
            Log.d(TAG, count + message);
            Toast.makeText(context,
                    count + message,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            message = " geo fences, exiting detected";
            Log.d(TAG, count + message);
            Toast.makeText(context,
                    count + message,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        else
            message = " error, unidentified geo fence transition detected. ";



        //test notification
        //notification is shown when the device receives geo fencing broadcast
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Mutify Geo fencing test")
                .setContentText(count + message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());
        Log.d(TAG, "pass");
    }
}