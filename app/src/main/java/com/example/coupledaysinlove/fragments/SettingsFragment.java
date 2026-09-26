package com.example.coupledaysinlove.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.dialogs.PinSetupDialog;
import com.example.coupledaysinlove.helpers.NotificationHelper;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.example.coupledaysinlove.widgets.CoupleWidgetProvider;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    private MaterialSwitch switchDarkMode;
    private MaterialSwitch switchWidgetDarkMode;
    private MaterialSwitch switchNotifications;
    private MaterialSwitch switchAnniversaryAllDay;

    private MaterialSwitch switchSecurityLock;
    private MaterialSwitch switchBiometric;
    private MaterialButtonToggleGroup groupLockFrequency;
    private View layoutSecuritySubOptions;
    private View layoutBiometricSwitch;
    private View btnChangePin;

    private boolean isUpdatingSecuritySwitches = false;

    private MaterialButtonToggleGroup groupWidgetPhotoSize;
    private MaterialButtonToggleGroup groupWidgetTextScale;

    private MaterialCardView cardThemePink;
    private MaterialCardView cardThemeRed;
    private MaterialCardView cardThemePurple;
    private MaterialCardView cardThemeBlue;
    private MaterialCardView cardThemeMint;

    private PreferencesHelper prefsHelper;

    private ActivityResultLauncher<String> permissionNotificationLauncher;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionNotificationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                prefsHelper.setNotificationsEnabled(true);
                NotificationHelper.scheduleAllNotifications(requireContext());
            } else {
                prefsHelper.setNotificationsEnabled(false);
                prefsHelper.setAnniversaryAllDayEnabled(false);
                switchNotifications.setChecked(false);
                switchAnniversaryAllDay.setChecked(false);
                switchAnniversaryAllDay.setEnabled(false);
                Toast.makeText(requireContext(), R.string.notification_permission_msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new PreferencesHelper(requireContext());

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchWidgetDarkMode = view.findViewById(R.id.switchWidgetDarkMode);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchAnniversaryAllDay = view.findViewById(R.id.switchAnniversaryAllDay);

        groupWidgetPhotoSize = view.findViewById(R.id.groupWidgetPhotoSize);
        groupWidgetTextScale = view.findViewById(R.id.groupWidgetTextScale);

        cardThemePink = view.findViewById(R.id.cardThemePink);
        cardThemeRed = view.findViewById(R.id.cardThemeRed);
        cardThemePurple = view.findViewById(R.id.cardThemePurple);
        cardThemeBlue = view.findViewById(R.id.cardThemeBlue);
        cardThemeMint = view.findViewById(R.id.cardThemeMint);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        switchWidgetDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setWidgetDarkModeEnabled(isChecked);
            CoupleWidgetProvider.updateAllWidgets(requireContext());
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                    openNotificationSettings();
                } else {
                    prefsHelper.setNotificationsEnabled(true);
                    NotificationHelper.scheduleAllNotifications(requireContext());
                }
            } else {
                prefsHelper.setNotificationsEnabled(false);
                prefsHelper.setAnniversaryAllDayEnabled(false);
                switchAnniversaryAllDay.setChecked(false);
            }
            switchAnniversaryAllDay.setEnabled(isChecked);
        });

        switchAnniversaryAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setAnniversaryAllDayEnabled(isChecked);
            if (prefsHelper.areNotificationsEnabled()) {
                NotificationHelper.scheduleAllNotifications(requireContext());
            }
        });

        setupThemeControls();
        setupWidgetControls();
        setupSecurityControls(view);
        loadValues();
    }

    private void setupSecurityControls(View view) {
        switchSecurityLock = view.findViewById(R.id.switchSecurityLock);
        switchBiometric = view.findViewById(R.id.switchBiometric);
        layoutSecuritySubOptions = view.findViewById(R.id.layoutSecuritySubOptions);
        layoutBiometricSwitch = view.findViewById(R.id.layoutBiometricSwitch);
        btnChangePin = view.findViewById(R.id.btnChangePin);

        if (switchSecurityLock != null) {
            switchSecurityLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isUpdatingSecuritySwitches) return;

                if (isChecked) {
                    if (!prefsHelper.hasSecurityPin()) {
                        showPinSetupDialog(PinSetupDialog.MODE_SET_PIN);
                    } else {
                        prefsHelper.setSecurityLockEnabled(true);
                        Toast.makeText(requireContext(), R.string.security_enabled_toast, Toast.LENGTH_SHORT).show();
                        loadValues();
                    }
                } else {
                    if (prefsHelper.isSecurityLockEnabled()) {
                        showPinSetupDialog(PinSetupDialog.MODE_VERIFY_PIN);
                    }
                }
            });
        }

        if (switchBiometric != null) {
            switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isUpdatingSecuritySwitches) return;
                prefsHelper.setBiometricEnabled(isChecked);
            });
        }

        if (btnChangePin != null) {
            btnChangePin.setOnClickListener(v -> showPinSetupDialog(PinSetupDialog.MODE_CHANGE_PIN));
        }

        groupLockFrequency = view.findViewById(R.id.groupLockFrequency);
        if (groupLockFrequency != null) {
            groupLockFrequency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked || isUpdatingSecuritySwitches) return;
                if (checkedId == R.id.btnFreqAlways) {
                    prefsHelper.setLockFrequency(PreferencesHelper.FREQ_ALWAYS);
                } else if (checkedId == R.id.btnFreqAppClose) {
                    prefsHelper.setLockFrequency(PreferencesHelper.FREQ_APP_CLOSE);
                } else if (checkedId == R.id.btnFreqOnceADay) {
                    prefsHelper.setLockFrequency(PreferencesHelper.FREQ_ONCE_A_DAY);
                }
            });
        }
    }

    private void showPinSetupDialog(int mode) {
        PinSetupDialog dialog = new PinSetupDialog(requireContext(), mode, new PinSetupDialog.PinSetupListener() {
            @Override
            public void onPinSetSuccessfully(String newPin) {
                prefsHelper.setSecurityLockEnabled(true);
                Toast.makeText(requireContext(), R.string.security_enabled_toast, Toast.LENGTH_SHORT).show();
                loadValues();
            }

            @Override
            public void onPinVerifiedSuccessfully() {
                if (mode == PinSetupDialog.MODE_VERIFY_PIN) {
                    prefsHelper.setSecurityLockEnabled(false);
                    Toast.makeText(requireContext(), R.string.security_disabled_toast, Toast.LENGTH_SHORT).show();
                    loadValues();
                }
            }

            @Override
            public void onPinSetupCanceled() {
                loadValues();
            }
        });
        dialog.show();
    }

    private boolean isBiometricAvailable() {
        if (getContext() == null) return false;
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void setupThemeControls() {
        if (cardThemePink != null) {
            cardThemePink.setOnClickListener(v -> selectTheme("pink", getString(R.string.theme_pink)));
        }
        if (cardThemeRed != null) {
            cardThemeRed.setOnClickListener(v -> selectTheme("red", getString(R.string.theme_red)));
        }
        if (cardThemePurple != null) {
            cardThemePurple.setOnClickListener(v -> selectTheme("purple", getString(R.string.theme_purple)));
        }
        if (cardThemeBlue != null) {
            cardThemeBlue.setOnClickListener(v -> selectTheme("blue", getString(R.string.theme_blue)));
        }
        if (cardThemeMint != null) {
            cardThemeMint.setOnClickListener(v -> selectTheme("mint", getString(R.string.theme_mint)));
        }
    }

    private void selectTheme(String themeKey, String themeName) {
        if (!prefsHelper.getAppTheme().equals(themeKey)) {
            prefsHelper.setAppTheme(themeKey);
            Toast.makeText(requireContext(), getString(R.string.theme_changed_toast, themeName), Toast.LENGTH_SHORT).show();
            requireActivity().recreate();
        }
    }

    private void setupWidgetControls() {
        if (groupWidgetPhotoSize != null) {
            groupWidgetPhotoSize.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                if (checkedId == R.id.btnWidgetPhotoSmall) {
                    prefsHelper.setWidgetPhotoSize(58);
                } else if (checkedId == R.id.btnWidgetPhotoMedium) {
                    prefsHelper.setWidgetPhotoSize(72);
                } else if (checkedId == R.id.btnWidgetPhotoLarge) {
                    prefsHelper.setWidgetPhotoSize(88);
                }
                CoupleWidgetProvider.updateAllWidgets(requireContext());
            });
        }

        if (groupWidgetTextScale != null) {
            groupWidgetTextScale.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                if (checkedId == R.id.btnWidgetTextSmall) {
                    prefsHelper.setWidgetTextScale(0.9f);
                } else if (checkedId == R.id.btnWidgetTextMedium) {
                    prefsHelper.setWidgetTextScale(1.15f);
                } else if (checkedId == R.id.btnWidgetTextLarge) {
                    prefsHelper.setWidgetTextScale(1.45f);
                }
                CoupleWidgetProvider.updateAllWidgets(requireContext());
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadValues();
    }

    private void loadValues() {
        switchDarkMode.setChecked(prefsHelper.isDarkModeEnabled());
        switchWidgetDarkMode.setChecked(prefsHelper.isWidgetDarkModeEnabled());

        boolean notificationsAreOn = prefsHelper.areNotificationsEnabled() && NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        switchNotifications.setChecked(notificationsAreOn);
        switchAnniversaryAllDay.setChecked(prefsHelper.isAnniversaryAllDayEnabled() && notificationsAreOn);
        switchAnniversaryAllDay.setEnabled(notificationsAreOn);

        if (groupWidgetPhotoSize != null) {
            int photoSize = prefsHelper.getWidgetPhotoSize();
            if (photoSize <= 62) {
                groupWidgetPhotoSize.check(R.id.btnWidgetPhotoSmall);
            } else if (photoSize >= 80) {
                groupWidgetPhotoSize.check(R.id.btnWidgetPhotoLarge);
            } else {
                groupWidgetPhotoSize.check(R.id.btnWidgetPhotoMedium);
            }
        }

        if (groupWidgetTextScale != null) {
            float textScale = prefsHelper.getWidgetTextScale();
            if (textScale < 1.0f) {
                groupWidgetTextScale.check(R.id.btnWidgetTextSmall);
            } else if (textScale > 1.3f) {
                groupWidgetTextScale.check(R.id.btnWidgetTextLarge);
            } else {
                groupWidgetTextScale.check(R.id.btnWidgetTextMedium);
            }
        }

        isUpdatingSecuritySwitches = true;
        if (switchSecurityLock != null) {
            boolean isLockOn = prefsHelper.isSecurityLockEnabled() && prefsHelper.hasSecurityPin();
            switchSecurityLock.setChecked(isLockOn);

            if (layoutSecuritySubOptions != null) {
                layoutSecuritySubOptions.setVisibility(isLockOn ? View.VISIBLE : View.GONE);
            }

            if (layoutBiometricSwitch != null) {
                boolean bioAvail = isBiometricAvailable();
                layoutBiometricSwitch.setVisibility(bioAvail ? View.VISIBLE : View.GONE);
            }

            if (switchBiometric != null) {
                switchBiometric.setChecked(prefsHelper.isBiometricEnabled());
            }

            if (groupLockFrequency != null) {
                String freq = prefsHelper.getLockFrequency();
                if (PreferencesHelper.FREQ_APP_CLOSE.equals(freq)) {
                    groupLockFrequency.check(R.id.btnFreqAppClose);
                } else if (PreferencesHelper.FREQ_ONCE_A_DAY.equals(freq)) {
                    groupLockFrequency.check(R.id.btnFreqOnceADay);
                } else {
                    groupLockFrequency.check(R.id.btnFreqAlways);
                }
            }
        }
        isUpdatingSecuritySwitches = false;

        updateThemeSelectionUI();
        CoupleWidgetProvider.updateAllWidgets(requireContext());
    }

    private void updateThemeSelectionUI() {
        String currentTheme = prefsHelper.getAppTheme();

        TypedValue primaryValue = new TypedValue();
        TypedValue strokeValue = new TypedValue();
        if (getContext() != null) {
            getContext().getTheme().resolveAttribute(R.attr.appPrimaryColor, primaryValue, true);
            getContext().getTheme().resolveAttribute(R.attr.appCardStrokeColor, strokeValue, true);
        }
        int primaryColor = primaryValue.data;
        int defaultStrokeColor = strokeValue.data;

        setupThemeCardHighlight(cardThemePink, "pink".equals(currentTheme), primaryColor, defaultStrokeColor);
        setupThemeCardHighlight(cardThemeRed, "red".equals(currentTheme), primaryColor, defaultStrokeColor);
        setupThemeCardHighlight(cardThemePurple, "purple".equals(currentTheme), primaryColor, defaultStrokeColor);
        setupThemeCardHighlight(cardThemeBlue, "blue".equals(currentTheme), primaryColor, defaultStrokeColor);
        setupThemeCardHighlight(cardThemeMint, "mint".equals(currentTheme), primaryColor, defaultStrokeColor);
    }

    private void setupThemeCardHighlight(MaterialCardView card, boolean isSelected, int primaryColor, int defaultStrokeColor) {
        if (card == null) return;
        if (isSelected) {
            card.setStrokeColor(primaryColor);
            card.setStrokeWidth((int) (3.5f * getResources().getDisplayMetrics().density));
        } else {
            card.setStrokeColor(defaultStrokeColor);
            card.setStrokeWidth((int) (1.2f * getResources().getDisplayMetrics().density));
        }
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}