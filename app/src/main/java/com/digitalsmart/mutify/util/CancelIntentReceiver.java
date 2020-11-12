package com.digitalsmart.mutify.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static com.digitalsmart.mutify.util.Constants.PACKAGE_NAME;

public class CancelIntentReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(PACKAGE_NAME + "_AUDIO_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("stop", true);
        editor.apply();
    }
}
