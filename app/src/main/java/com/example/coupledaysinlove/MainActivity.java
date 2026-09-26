package com.example.coupledaysinlove;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.coupledaysinlove.fragments.CounterFragment;
import com.example.coupledaysinlove.fragments.EventsFragment;
import com.example.coupledaysinlove.fragments.ProfileFragment;
import com.example.coupledaysinlove.fragments.SettingsFragment;
import com.example.coupledaysinlove.helpers.NotificationHelper;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper prefs = new PreferencesHelper(this);
        boolean isDark = prefs.isDarkModeEnabled();

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        setTheme(PreferencesHelper.getThemeResourceId(prefs.getAppTheme()));

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(
                this,
                isDark ? SystemBarStyle.dark(Color.TRANSPARENT) : SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                isDark ? SystemBarStyle.dark(Color.TRANSPARENT) : SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_main);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(!isDark);
        windowInsetsController.setAppearanceLightNavigationBars(!isDark);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CounterFragment(), "counter")
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;
            String tag = null;

            if (itemId == R.id.nav_counter) {
                targetFragment = new CounterFragment();
                tag = "counter";
            } else if (itemId == R.id.nav_events) {
                targetFragment = new EventsFragment();
                tag = "events";
            } else if (itemId == R.id.nav_profile) {
                targetFragment = new ProfileFragment();
                tag = "profile";
            } else if (itemId == R.id.nav_settings) {
                targetFragment = new SettingsFragment();
                tag = "settings";
            }

            if (targetFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, targetFragment, tag)
                        .commit();
                return true;
            }
            return false;
        });

        checkNotificationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSecurityLock();
    }

    private void checkSecurityLock() {
        PreferencesHelper prefs = new PreferencesHelper(this);
        if (prefs.isSecurityLockEnabled() && prefs.hasSecurityPin()) {
            String freq = prefs.getLockFrequency();
            boolean needsLock = false;

            if (PreferencesHelper.FREQ_ONCE_A_DAY.equals(freq)) {
                String today = getTodayDateString();
                if (!today.equals(prefs.getLastAuthDate())) {
                    needsLock = true;
                }
            } else if (PreferencesHelper.FREQ_APP_CLOSE.equals(freq)) {
                if (!SecurityLockActivity.isAppUnlocked) {
                    needsLock = true;
                }
            } else {
                if (!SecurityLockActivity.isAppUnlocked) {
                    needsLock = true;
                }
            }

            if (needsLock) {
                SecurityLockActivity.start(this);
            }
        }
    }

    private String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations()) {
            PreferencesHelper prefs = new PreferencesHelper(this);
            String freq = prefs.getLockFrequency();
            if (PreferencesHelper.FREQ_ALWAYS.equals(freq)) {
                SecurityLockActivity.isAppUnlocked = false;
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_NOTIFICATIONS
                );
            } else {
                NotificationHelper.scheduleAllNotifications(this);
            }
        } else {
            NotificationHelper.scheduleAllNotifications(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.scheduleAllNotifications(this);
            }
        }
    }
}