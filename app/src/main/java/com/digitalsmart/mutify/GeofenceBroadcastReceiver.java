package com.digitalsmart.mutify;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
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
    private SharedPreferences audioSettingSave;
    private Context context;
    private AudioManager audioManager;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private Thread counterThread;
    private final int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioSettingSave = context.getSharedPreferences(PACKAGE_NAME + "_AUDIO_KEY", Context.MODE_PRIVATE);

        builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Mutify")
                .setContentText("In progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        counterThread = new Thread(() -> {
            for (int PROGRESS_CURRENT1 = 0; PROGRESS_CURRENT1 < PROGRESS_MAX; PROGRESS_CURRENT1 += 10)
            {
                SystemClock.sleep(1000);
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT1,false);
                notificationManager.notify(NOTIFICATION_ID, builder.setNotificationSilent().build());
            }
            builder.setProgress(0, 0, false);
            builder.setContentText("Phone mutified.");
            notificationManager.notify(NOTIFICATION_ID, builder.setNotificationSilent().build());

            //change audio setting to vibrate
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        });




        if (intent.getAction()!= null)
        {
            if (counterThread.isAlive() && intent.getAction().equals(PACKAGE_NAME + "_cancel"))
            {
                counterThread.interrupt();
                //todo: the notification message doesn't clear, need fix
                notificationManager.cancelAll();
            }
        }

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
            message = " geo fences, dwelling detected";
            Log.d(TAG, count + message);
            if (count > 0)
                turnOnVibrate();
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
            restoreAudioSettings();
        }

        Log.d(TAG, "pass");
    }



    //turn on vibrate - "mutify"
    private void turnOnVibrate()
    {
        //save the previous audio settings
        SharedPreferences.Editor editor = audioSettingSave.edit();
        editor.putInt(PACKAGE_NAME + "_AUDIO_SETTINGS", audioManager.getRingerMode()).apply();

        if (!audioManager.isVolumeFixed())
        {
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE)
            {
                Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
                intent.setAction(PACKAGE_NAME + "_cancel");
                PendingIntent pIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
                builder.addAction(R.drawable.location_icon, "Cancel",pIntent);
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
                counterThread.start();
            }
        }
    }


    //restore the audio settings from before the phone is muted
    private void restoreAudioSettings()
    {
        int audioCode = audioSettingSave.getInt(PACKAGE_NAME + "_AUDIO_SETTINGS", -99);
        if (audioCode != -99)
        {
            audioManager.setRingerMode(audioCode);
            builder = new NotificationCompat.Builder(context, PACKAGE_NAME)
                    .setSmallIcon(R.drawable.location_icon)
                    .setContentTitle("Mutify")
                    .setContentText("Audio settings restored.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

}