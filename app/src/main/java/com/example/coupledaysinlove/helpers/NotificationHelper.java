package com.example.coupledaysinlove.helpers;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.coupledaysinlove.MainActivity;
import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.models.SpecialEvent;
import com.example.coupledaysinlove.receivers.NotificationReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationHelper {

    public static final String CHANNEL_ID = "couple_events_channel";
    public static final String CHANNEL_NAME = "Recordatorios de Pareja";

    private static final ExecutorService NOTIFICATION_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para aniversarios, cumpleaños y fechas especiales");
            channel.enableVibration(true);
            channel.setLightColor(0xFFD81B60);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId, boolean isAllDay) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_counter)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setOngoing(isAllDay)
                .setAutoCancel(!isAllDay);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        try {
            manager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void checkAndSendMissedNotifications(Context context) {
        PreferencesHelper prefs = new PreferencesHelper(context);
        if (!prefs.areNotificationsEnabled()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        // Check Anniversary
        if (prefs.hasStartDate()) {
            long startMillis = prefs.getStartDateMillis();
            long daysLeft = DateUtils.getDaysUntilNextOccurrence(startMillis);

            if (daysLeft == 0) {
                String key = "anniversary_0_" + todayStr;
                if (!todayStr.equals(prefs.getLastNotifiedDate(key))) {
                    prefs.setLastNotifiedDate(key, todayStr);
                    boolean isAllDay = prefs.isAnniversaryAllDayEnabled();
                    showNotification(context, "[Aniversario] ¡Hoy es su Aniversario! 🎉❤️", "Categoría: Aniversario • 0 días faltantes - ¡Hoy celebran su Aniversario juntos! 💕", 1000, isAllDay);
                }
            } else if (daysLeft == 1) {
                String key = "anniversary_1_" + todayStr;
                if (!todayStr.equals(prefs.getLastNotifiedDate(key))) {
                    prefs.setLastNotifiedDate(key, todayStr);
                    showNotification(context, "[Aniversario] Falta 1 día ❤️", "Categoría: Aniversario • Falta exactamente 1 día para su Aniversario.", 1001, false);
                }
            }
        }

        // Check Special Events
        List<SpecialEvent> events = prefs.getEvents();
        for (int i = 0; i < events.size(); i++) {
            SpecialEvent event = events.get(i);
            long daysLeft = DateUtils.getDaysUntilNextOccurrence(event.getDateMillis());
            String cat = (event.getCategory() != null && !event.getCategory().isEmpty()) ? event.getCategory() : "Evento";

            if (daysLeft == 0) {
                String key = "event_0_" + event.getId() + "_" + todayStr;
                if (!todayStr.equals(prefs.getLastNotifiedDate(key))) {
                    prefs.setLastNotifiedDate(key, todayStr);
                    showNotification(context, "[" + cat + "] ¡Hoy es el día especial! 🎉❤️", "Categoría: " + cat + " • 0 días faltantes - ¡Hoy es " + event.getTitle() + "!", 2000 + i, false);
                }
            } else if (daysLeft == 1) {
                String key = "event_1_" + event.getId() + "_" + todayStr;
                if (!todayStr.equals(prefs.getLastNotifiedDate(key))) {
                    prefs.setLastNotifiedDate(key, todayStr);
                    showNotification(context, "[" + cat + "] Falta 1 día ❤️", "Categoría: " + cat + " • Falta exactamente 1 día para " + event.getTitle() + ".", 2000 + i + 1, false);
                }
            }
        }
    }

    public static void scheduleAllNotifications(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        NOTIFICATION_EXECUTOR.execute(() -> scheduleAllNotificationsInternal(appContext));
    }

    private static void scheduleAllNotificationsInternal(Context context) {
        PreferencesHelper prefs = new PreferencesHelper(context);
        if (!prefs.areNotificationsEnabled()) {
            return;
        }

        createNotificationChannel(context);
        checkAndSendMissedNotifications(context);

        int[] milestones = new int[]{50, 10, 5, 1, 0};

        // 1. Schedule Anniversary Milestone Notifications
        if (prefs.hasStartDate()) {
            long startMillis = prefs.getStartDateMillis();
            long daysUntilAnniversary = DateUtils.getDaysUntilNextOccurrence(startMillis);

            for (int m : milestones) {
                if (daysUntilAnniversary >= m) {
                    long triggerInDays = daysUntilAnniversary - m;
                    int reqCode = 1000 + m;
                    String title;
                    String msg;
                    boolean isAllDay = (m == 0) && prefs.isAnniversaryAllDayEnabled();

                    switch (m) {
                        case 50:
                            title = "[Aniversario] Faltan 50 días ⏳";
                            msg = "Categoría: Aniversario • Faltan exactamente 50 días para su Aniversario.";
                            break;
                        case 10:
                            title = "[Aniversario] Faltan 10 días 🎁";
                            msg = "Categoría: Aniversario • Faltan exactamente 10 días para su Aniversario.";
                            break;
                        case 5:
                            title = "[Aniversario] Faltan 5 días 🎉";
                            msg = "Categoría: Aniversario • Faltan exactamente 5 días para su Aniversario.";
                            break;
                        case 1:
                            title = "[Aniversario] Falta 1 día ❤️";
                            msg = "Categoría: Aniversario • Falta exactamente 1 día para su Aniversario.";
                            break;
                        case 0:
                        default:
                            title = "[Aniversario] ¡Hoy es su Aniversario! 🎉❤️";
                            msg = "Categoría: Aniversario • 0 días faltantes - ¡Hoy celebran su Aniversario juntos! 💕";
                            break;
                    }

                    if (m == 0 && isAllDay) {
                        scheduleAlarmAtHour(context, reqCode + 6, title, msg, triggerInDays, 6, true);
                        scheduleAlarmAtHour(context, reqCode + 12, title, msg, triggerInDays, 12, true);
                        scheduleAlarmAtHour(context, reqCode + 18, title, msg, triggerInDays, 18, true);
                    } else {
                        scheduleAlarmAtHour(context, reqCode, title, msg, triggerInDays, 9, false);
                    }
                }
            }
        }

        // 2. Schedule Special Events Milestone Notifications
        List<SpecialEvent> events = prefs.getEvents();
        for (int i = 0; i < events.size(); i++) {
            SpecialEvent event = events.get(i);
            long daysLeft = DateUtils.getDaysUntilNextOccurrence(event.getDateMillis());
            String cat = (event.getCategory() != null && !event.getCategory().isEmpty()) ? event.getCategory() : "Evento";

            for (int m : milestones) {
                if (daysLeft >= m) {
                    long triggerInDays = daysLeft - m;
                    int reqCode = 2000 + (i * 10) + m;
                    String title;
                    String msg;

                    switch (m) {
                        case 50:
                            title = "[" + cat + "] Faltan 50 días ⏳";
                            msg = "Categoría: " + cat + " • Faltan exactamente 50 días para " + event.getTitle() + ".";
                            break;
                        case 10:
                            title = "[" + cat + "] Faltan 10 días 🎁";
                            msg = "Categoría: " + cat + " • Faltan exactamente 10 días para " + event.getTitle() + ".";
                            break;
                        case 5:
                            title = "[" + cat + "] Faltan 5 días 🎉";
                            msg = "Categoría: " + cat + " • Faltan exactamente 5 días para " + event.getTitle() + ".";
                            break;
                        case 1:
                            title = "[" + cat + "] Falta 1 día ❤️";
                            msg = "Categoría: " + cat + " • Falta exactamente 1 día para " + event.getTitle() + ".";
                            break;
                        case 0:
                        default:
                            title = "[" + cat + "] ¡Hoy es el día especial! 🎉❤️";
                            msg = "Categoría: " + cat + " • 0 días faltantes - ¡Hoy es " + event.getTitle() + "!";
                            break;
                    }

                    if (m == 0 && prefs.isAnniversaryAllDayEnabled()) {
                        scheduleAlarmAtHour(context, reqCode + 6, title, msg, triggerInDays, 6, true);
                        scheduleAlarmAtHour(context, reqCode + 12, title, msg, triggerInDays, 12, true);
                        scheduleAlarmAtHour(context, reqCode + 18, title, msg, triggerInDays, 18, true);
                    } else {
                        scheduleAlarmAtHour(context, reqCode, title, msg, triggerInDays, 9, false);
                    }
                }
            }
        }
    }

    private static void scheduleAlarmAtHour(Context context, int requestCode, String title, String message, long daysInFuture, int hourOfDay, boolean isAllDay) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TITLE, title);
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(NotificationReceiver.EXTRA_ID, requestCode);
        intent.putExtra(NotificationReceiver.EXTRA_IS_ALL_DAY, isAllDay);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar triggerCal = Calendar.getInstance();
        triggerCal.add(Calendar.DAY_OF_YEAR, (int) daysInFuture);
        triggerCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        triggerCal.set(Calendar.MINUTE, 0);
        triggerCal.set(Calendar.SECOND, 0);
        triggerCal.set(Calendar.MILLISECOND, 0);

        long triggerTime = triggerCal.getTimeInMillis();

        if (triggerTime <= System.currentTimeMillis()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}