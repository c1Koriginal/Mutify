package com.digitalsmart.mutify.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.digitalsmart.mutify.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.digitalsmart.mutify.util.Constants.NOTIFICATION_ID;
import static com.digitalsmart.mutify.util.Constants.PACKAGE_NAME;

public class GeofenceBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "broadcast";
    private SharedPreferences mutifySharedPreferences;
    private Context context;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private final int PROGRESS_MAX = 100;
    private boolean stopThread = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;
        createNotificationChannel();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Mutify")
                .setContentText("In progress")
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
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
            //todo: change to dwelling
            message = " geo fences, dwelling detected";
            Log.d(TAG, count + message);
            //turnOnDND();
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            message = " geo fences, entering detected";
            Log.d(TAG, count + message);
            turnOnDND();

        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            message = " geo fences, exiting detected";
            Log.d(TAG, count + message);
            restoreAudioSettings();
        }
        Log.d(TAG, "pass");
    }



    //turn on vibrate - "mutify"
    private void turnOnDND()
    {
        createNotificationChannel();
        //save the previous audio settings
        mutifySharedPreferences = context.getSharedPreferences(PACKAGE_NAME + "_AUDIO_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mutifySharedPreferences.edit();
        editor.putInt(PACKAGE_NAME + "_AUDIO_SETTINGS", notificationManager.getCurrentInterruptionFilter());
        editor.apply();

        if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS)
            {
                int PROGRESS_CURRENT = 0;
                Intent intent = new Intent(context, CancelIntentReceiver.class);
                intent.setAction(PACKAGE_NAME + "_cancel");
                PendingIntent pIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
                builder.addAction(R.drawable.location_icon, "Cancel",pIntent);
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                notificationManager.notify(NOTIFICATION_ID, builder.build());


                Thread counterThread = new Thread(() -> {
                    for (int PROGRESS_CURRENT1 = 0; PROGRESS_CURRENT1 < PROGRESS_MAX; PROGRESS_CURRENT1 += 10)
                    {
                        stopThread = mutifySharedPreferences.getBoolean("stop", false);

                        if (stopThread) {
                            editor.putBoolean("stop", false);
                            editor.apply();
                            notificationManager.cancelAll();
                            return;
                        }

                        SystemClock.sleep(1000);
                        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT1,false);
                        notificationManager.notify(NOTIFICATION_ID, builder
                                .setColorized(true)
                                .setNotificationSilent()
                                .setPriority(2)
                                .setOngoing(true)
                                .build());
                    }

                    //turn on do not disturb
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);

                    builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                            .setSmallIcon(R.drawable.location_icon)
                            .setContentTitle("Mutify")
                            .setContentText("Phone mutified.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(NOTIFICATION_ID, builder.setPriority(2).build());
                    editor.putString(PACKAGE_NAME+"_CHANGED", "true");
                    editor.apply();
                    Log.d(TAG, "settings changed: " + mutifySharedPreferences.getString(PACKAGE_NAME+"_CHANGED", "unknown"));
                });
                counterThread.start();

            }
            else
            {
                editor.putString(PACKAGE_NAME+"_CHANGED", "false");
                editor.apply();
            }
    }


    //restore the audio settings from before the phone is muted
    private void restoreAudioSettings()
    {
        createNotificationChannel();
        mutifySharedPreferences = context.getSharedPreferences(PACKAGE_NAME + "_AUDIO_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mutifySharedPreferences.edit();
        String changed = mutifySharedPreferences.getString(PACKAGE_NAME+"_CHANGED", "unknown");

        if (notificationManager.getCurrentInterruptionFilter()!= NotificationManager.INTERRUPTION_FILTER_ALARMS)
        {
            editor.putInt(PACKAGE_NAME + "_AUDIO_SETTINGS", notificationManager.getCurrentInterruptionFilter());
            editor.apply();
        }
        if (changed.equals("true") && notificationManager.getCurrentInterruptionFilter()== NotificationManager.INTERRUPTION_FILTER_ALARMS)
        {
            int audioCode = mutifySharedPreferences.getInt(PACKAGE_NAME + "_AUDIO_SETTINGS", -99);
            if (audioCode != -99)
            {
                notificationManager.setInterruptionFilter(audioCode);
                Log.d(TAG, "settings restored to: " + audioCode);
                builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                        .setSmallIcon(R.drawable.location_icon)
                        .setContentTitle("Mutify")
                        .setContentText("Audio settings restored.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
        editor.putInt(PACKAGE_NAME + "_AUDIO_SETTINGS", notificationManager.getCurrentInterruptionFilter());
        editor.apply();
    }


    //create a notification channel, feel free to call this method repeatedly
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
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}