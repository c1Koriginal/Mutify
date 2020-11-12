package com.digitalsmart.mutify.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.digitalsmart.mutify.UserDataManager;

//restore geofences after the device reboots
public class RebootBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            new UserDataManager(context).addFencesAfterReboot(context);
        }
    }
}
