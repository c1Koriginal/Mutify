package com.digitalsmart.mutify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CancelIntentReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("CANCER", String.valueOf(Thread.currentThread().getId()));
    }
}
