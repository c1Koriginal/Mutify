package com.digitalsmart.mutify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;

public class CancelIntentReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPref = context.getSharedPreferences("stop", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        boolean stopThread = true;

        editor.putBoolean("stop", stopThread);
        editor.apply();

        Log.d("CANCER", String.valueOf(Thread.currentThread().getId()));
    }
}
