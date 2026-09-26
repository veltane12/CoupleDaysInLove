package com.example.coupledaysinlove.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.coupledaysinlove.helpers.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_TITLE = "notification_title";
    public static final String EXTRA_MESSAGE = "notification_message";
    public static final String EXTRA_ID = "notification_id";
    public static final String EXTRA_IS_ALL_DAY = "notification_is_all_day";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        int id = intent.getIntExtra(EXTRA_ID, (int) System.currentTimeMillis());
        boolean isAllDay = intent.getBooleanExtra(EXTRA_IS_ALL_DAY, false);

        if (title == null) title = "Día Especial ❤️";
        if (message == null) message = "¡Hoy tienen un evento especial juntos!";

        // Show the notification
        NotificationHelper.showNotification(context, title, message, id, isAllDay);

        // Reschedule notifications in background
        NotificationHelper.scheduleAllNotifications(context);
    }
}