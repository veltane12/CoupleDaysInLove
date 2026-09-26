package com.example.coupledaysinlove.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.coupledaysinlove.helpers.NotificationHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            ConnectivityManager.CONNECTIVITY_ACTION.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            NotificationHelper.checkAndSendMissedNotifications(context);
            NotificationHelper.scheduleAllNotifications(context);
        }
    }
}