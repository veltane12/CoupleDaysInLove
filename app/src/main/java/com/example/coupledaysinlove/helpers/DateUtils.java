package com.example.coupledaysinlove.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static long getDaysTogether(long startMillis) {
        long nowMillis = System.currentTimeMillis();
        if (startMillis > nowMillis) {
            return 0;
        }
        long diffMillis = nowMillis - startMillis;
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    public static String getFormattedDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public static int getAgeTurning(long birthMillis) {
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTimeInMillis(birthMillis);
        int birthYear = birthCal.get(Calendar.YEAR);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar nextEvent = Calendar.getInstance();
        nextEvent.set(Calendar.YEAR, today.get(Calendar.YEAR));
        nextEvent.set(Calendar.MONTH, birthCal.get(Calendar.MONTH));
        nextEvent.set(Calendar.DAY_OF_MONTH, birthCal.get(Calendar.DAY_OF_MONTH));
        nextEvent.set(Calendar.HOUR_OF_DAY, 0);
        nextEvent.set(Calendar.MINUTE, 0);
        nextEvent.set(Calendar.SECOND, 0);
        nextEvent.set(Calendar.MILLISECOND, 0);

        if (nextEvent.before(today)) {
            nextEvent.add(Calendar.YEAR, 1);
        }

        int nextYear = nextEvent.get(Calendar.YEAR);
        int age = nextYear - birthYear;
        return Math.max(0, age);
    }

    public static String getEventDisplayDate(long millis, String category) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd 'de' MMMM", Locale.getDefault());

        if ("Cumpleaños".equalsIgnoreCase(category)) {
            int age = getAgeTurning(millis);
            if (age > 0) {
                return "¡Cumplirá " + age + " años! • " + sdfDate.format(new Date(millis));
            } else {
                return "Cada " + sdfDate.format(new Date(millis));
            }
        } else if ("Aniversario".equalsIgnoreCase(category)) {
            return "Cada " + sdfDate.format(new Date(millis));
        } else {
            return getFormattedDate(millis);
        }
    }

    public static String getShortDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public static String getDetailedDuration(long startMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMillis);

        Calendar today = Calendar.getInstance();

        if (start.after(today)) {
            return "0 días";
        }

        int years = today.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int days = today.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);

        if (days < 0) {
            months--;
            Calendar prevMonth = (Calendar) today.clone();
            prevMonth.add(Calendar.MONTH, -1);
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        if (months < 0) {
            years--;
            months += 12;
        }

        StringBuilder duration = new StringBuilder();
        if (years > 0) {
            duration.append(years).append(years == 1 ? " año" : " años");
        }
        if (months > 0) {
            if (duration.length() > 0) duration.append(", ");
            duration.append(months).append(months == 1 ? " mes" : " meses");
        }
        if (days > 0 || duration.length() == 0) {
            if (duration.length() > 0) duration.append(", ");
            duration.append(days).append(days == 1 ? " día" : " días");
        }

        return duration.toString();
    }

    public static long getDaysUntilNextOccurrence(long eventMillis) {
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTimeInMillis(eventMillis);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar nextEvent = Calendar.getInstance();
        nextEvent.set(Calendar.YEAR, today.get(Calendar.YEAR));
        nextEvent.set(Calendar.MONTH, eventCal.get(Calendar.MONTH));
        nextEvent.set(Calendar.DAY_OF_MONTH, eventCal.get(Calendar.DAY_OF_MONTH));
        nextEvent.set(Calendar.HOUR_OF_DAY, 0);
        nextEvent.set(Calendar.MINUTE, 0);
        nextEvent.set(Calendar.SECOND, 0);
        nextEvent.set(Calendar.MILLISECOND, 0);

        if (nextEvent.before(today)) {
            nextEvent.add(Calendar.YEAR, 1);
        }

        long diff = nextEvent.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
}