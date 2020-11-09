package com.digitalsmart.mutify;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.digitalsmart.mutify.util.Constants.NOTIFICATION_ID;
import static com.digitalsmart.mutify.util.Constants.PACKAGE_NAME;


//todo: add methods here to show notification
public class GeofenceBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "broadcast";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;
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
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            message = " geo fences, entering detected";
            Log.d(TAG, count + message);
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            message = " geo fences, exiting detected";
            Log.d(TAG, count + message);
        }


        //test notification
        //notification is shown when the device receives geo fencing broadcast
        createNotification();
        Log.d(TAG, "pass");
    }



    public void createNotification()
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Mutify Geo fencing test")
                .setContentText("In progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent("Cancel");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        builder.addAction(R.drawable.location_icon, "Cancel",pIntent);


        final int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Thread thread = new Thread(() -> {
            for (int PROGRESS_CURRENT1 = 0; PROGRESS_CURRENT1 < PROGRESS_MAX; PROGRESS_CURRENT1 += 10){
                SystemClock.sleep(1000);
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT1,false);
                notificationManager.notify(NOTIFICATION_ID, builder.setNotificationSilent().build());
            }
            builder.setProgress(0, 0, false);
            builder.setContentText("New Location has been added");
            notificationManager.notify(NOTIFICATION_ID, builder.setNotificationSilent().build());
        });
        thread.start();
    }
}