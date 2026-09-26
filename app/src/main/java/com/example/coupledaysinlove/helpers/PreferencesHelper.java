package com.example.coupledaysinlove.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.models.BucketItem;
import com.example.coupledaysinlove.models.SpecialEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PreferencesHelper {
    private static final String PREF_NAME = "couple_prefs";
    private static final String KEY_PARTNER1 = "partner1_name";
    private static final String KEY_PARTNER2 = "partner2_name";
    private static final String KEY_PARTNER1_PHOTO = "partner1_photo_path";
    private static final String KEY_PARTNER2_PHOTO = "partner2_photo_path";
    private static final String KEY_START_DATE = "start_date_millis";
    private static final String KEY_EVENTS_JSON = "events_json";
    private static final String KEY_BUCKET_LIST_JSON = "bucket_list_json";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_ANNIVERSARY_ALL_DAY = "anniversary_all_day_enabled";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_APP_THEME = "app_theme";
    private static final String KEY_WIDGET_DARK_MODE = "widget_dark_mode_enabled";
    private static final String KEY_WIDGET_PHOTO_SIZE = "widget_photo_size";
    private static final String KEY_WIDGET_TEXT_SCALE = "widget_text_scale";
    public static final String FREQ_ALWAYS = "always";
    public static final String FREQ_APP_CLOSE = "app_close";
    public static final String FREQ_ONCE_A_DAY = "once_a_day";

    private static final String KEY_SECURITY_LOCK_ENABLED = "security_lock_enabled";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_SECURITY_PIN = "security_pin";
    private static final String KEY_SECURITY_LOCK_FREQUENCY = "security_lock_frequency";
    private static final String KEY_LAST_AUTH_DATE = "last_auth_date";

    private final SharedPreferences prefs;

    public PreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSecurityLockEnabled() {
        return prefs.getBoolean(KEY_SECURITY_LOCK_ENABLED, false);
    }

    public void setSecurityLockEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SECURITY_LOCK_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public String getSecurityPin() {
        return prefs.getString(KEY_SECURITY_PIN, null);
    }

    public void setSecurityPin(String pin) {
        prefs.edit().putString(KEY_SECURITY_PIN, pin).apply();
    }

    public boolean hasSecurityPin() {
        String pin = getSecurityPin();
        return pin != null && pin.length() == 4;
    }

    public void clearSecurityPin() {
        prefs.edit().remove(KEY_SECURITY_PIN).apply();
    }

    public String getLockFrequency() {
        return prefs.getString(KEY_SECURITY_LOCK_FREQUENCY, FREQ_ALWAYS);
    }

    public void setLockFrequency(String frequency) {
        prefs.edit().putString(KEY_SECURITY_LOCK_FREQUENCY, frequency).apply();
    }

    public String getLastAuthDate() {
        return prefs.getString(KEY_LAST_AUTH_DATE, "");
    }

    public void setLastAuthDate(String dateStr) {
        prefs.edit().putString(KEY_LAST_AUTH_DATE, dateStr).apply();
    }

    public String getPartner1Name() {
        return prefs.getString(KEY_PARTNER1, "Persona 1");
    }

    public void setPartner1Name(String name) {
        prefs.edit().putString(KEY_PARTNER1, name).apply();
    }

    public String getPartner2Name() {
        return prefs.getString(KEY_PARTNER2, "Persona 2");
    }

    public void setPartner2Name(String name) {
        prefs.edit().putString(KEY_PARTNER2, name).apply();
    }

    public String getPartner1PhotoPath() {
        return prefs.getString(KEY_PARTNER1_PHOTO, null);
    }

    public void setPartner1PhotoPath(String path) {
        prefs.edit().putString(KEY_PARTNER1_PHOTO, path).apply();
    }

    public String getPartner2PhotoPath() {
        return prefs.getString(KEY_PARTNER2_PHOTO, null);
    }

    public void setPartner2PhotoPath(String path) {
        prefs.edit().putString(KEY_PARTNER2_PHOTO, path).apply();
    }

    public boolean hasStartDate() {
        return getStartDateMillis() > 0;
    }

    public long getStartDateMillis() {
        return prefs.getLong(KEY_START_DATE, 0L);
    }

    public void setStartDateMillis(long millis) {
        prefs.edit().putLong(KEY_START_DATE, millis).apply();
    }

    public void clearStartDate() {
        prefs.edit().remove(KEY_START_DATE).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean isAnniversaryAllDayEnabled() {
        return prefs.getBoolean(KEY_ANNIVERSARY_ALL_DAY, true);
    }

    public void setAnniversaryAllDayEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANNIVERSARY_ALL_DAY, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public String getAppTheme() {
        return prefs.getString(KEY_APP_THEME, "pink");
    }

    public void setAppTheme(String themeKey) {
        prefs.edit().putString(KEY_APP_THEME, themeKey).apply();
    }

    public static int getThemeResourceId(String themeKey) {
        if (themeKey == null) return R.style.Theme_CoupleDaysInLove_Pink;
        switch (themeKey) {
            case "red":
                return R.style.Theme_CoupleDaysInLove_Red;
            case "purple":
                return R.style.Theme_CoupleDaysInLove_Purple;
            case "blue":
                return R.style.Theme_CoupleDaysInLove_Blue;
            case "mint":
                return R.style.Theme_CoupleDaysInLove_Mint;
            case "pink":
            default:
                return R.style.Theme_CoupleDaysInLove_Pink;
        }
    }

    public boolean isWidgetDarkModeEnabled() {
        return prefs.getBoolean(KEY_WIDGET_DARK_MODE, false);
    }

    public void setWidgetDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WIDGET_DARK_MODE, enabled).apply();
    }

    public int getWidgetPhotoSize() {
        return prefs.getInt(KEY_WIDGET_PHOTO_SIZE, 68);
    }

    public void setWidgetPhotoSize(int sizeDp) {
        prefs.edit().putInt(KEY_WIDGET_PHOTO_SIZE, sizeDp).apply();
    }

    public float getWidgetTextScale() {
        return prefs.getFloat(KEY_WIDGET_TEXT_SCALE, 1.1f);
    }

    public void setWidgetTextScale(float scale) {
        prefs.edit().putFloat(KEY_WIDGET_TEXT_SCALE, scale).apply();
    }

    public String getLastNotifiedDate(String key) {
        return prefs.getString("last_notified_" + key, "");
    }

    public void setLastNotifiedDate(String key, String dateStr) {
        prefs.edit().putString("last_notified_" + key, dateStr).apply();
    }

    public List<SpecialEvent> getEvents() {
        List<SpecialEvent> list = new ArrayList<>();
        String json = prefs.getString(KEY_EVENTS_JSON, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String photoPath = obj.has("photoPath") && !obj.isNull("photoPath") ? obj.getString("photoPath") : null;
                String description = obj.has("description") && !obj.isNull("description") ? obj.getString("description") : null;
                SpecialEvent event = new SpecialEvent(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getLong("dateMillis"),
                        obj.getString("category"),
                        obj.optBoolean("repeatYearly", true),
                        photoPath,
                        description
                );
                list.add(event);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveEvent(SpecialEvent event) {
        List<SpecialEvent> current = getEvents();
        int existingIndex = -1;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId().equals(event.getId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            current.set(existingIndex, event);
        } else {
            current.add(event);
        }
        saveEventsList(current);
    }

    public void deleteEvent(String eventId) {
        List<SpecialEvent> current = getEvents();
        List<SpecialEvent> updated = new ArrayList<>();
        for (SpecialEvent event : current) {
            if (!event.getId().equals(eventId)) {
                updated.add(event);
            } else {
                if (event.getPhotoPath() != null) {
                    try {
                        File file = new File(event.getPhotoPath());
                        if (file.exists()) file.delete();
                    } catch (Exception ignored) {}
                }
            }
        }
        saveEventsList(updated);
    }

    private void saveEventsList(List<SpecialEvent> list) {
        JSONArray array = new JSONArray();
        for (SpecialEvent event : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", event.getId());
                obj.put("title", event.getTitle());
                obj.put("dateMillis", event.getDateMillis());
                obj.put("category", event.getCategory());
                obj.put("repeatYearly", event.isRepeatYearly());
                if (event.getPhotoPath() != null) {
                    obj.put("photoPath", event.getPhotoPath());
                }
                if (event.getDescription() != null) {
                    obj.put("description", event.getDescription());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_EVENTS_JSON, array.toString()).apply();
    }

    public List<BucketItem> getBucketItems() {
        List<BucketItem> list = new ArrayList<>();
        String json = prefs.getString(KEY_BUCKET_LIST_JSON, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                BucketItem item = new BucketItem(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.optString("category", "Otro"),
                        obj.optBoolean("completed", false),
                        obj.optLong("completedDateMillis", 0L),
                        obj.optLong("createdDateMillis", System.currentTimeMillis())
                );
                list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveBucketItem(BucketItem item) {
        List<BucketItem> current = getBucketItems();
        int existingIndex = -1;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId().equals(item.getId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            current.set(existingIndex, item);
        } else {
            current.add(item);
        }
        saveBucketItemsList(current);
    }

    public void deleteBucketItem(String itemId) {
        List<BucketItem> current = getBucketItems();
        List<BucketItem> updated = new ArrayList<>();
        for (BucketItem item : current) {
            if (!item.getId().equals(itemId)) {
                updated.add(item);
            }
        }
        saveBucketItemsList(updated);
    }

    public void toggleBucketItemCompleted(String itemId, boolean completed, long completionDateMillis) {
        List<BucketItem> current = getBucketItems();
        for (BucketItem item : current) {
            if (item.getId().equals(itemId)) {
                item.setCompleted(completed);
                item.setCompletedDateMillis(completed ? completionDateMillis : 0L);
                break;
            }
        }
        saveBucketItemsList(current);
    }

    private void saveBucketItemsList(List<BucketItem> list) {
        JSONArray array = new JSONArray();
        for (BucketItem item : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", item.getId());
                obj.put("title", item.getTitle());
                obj.put("category", item.getCategory());
                obj.put("completed", item.isCompleted());
                obj.put("completedDateMillis", item.getCompletedDateMillis());
                obj.put("createdDateMillis", item.getCreatedDateMillis());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_BUCKET_LIST_JSON, array.toString()).apply();
    }

    public static String saveImageToInternalStorage(Context context, Uri sourceUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return null;

            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}