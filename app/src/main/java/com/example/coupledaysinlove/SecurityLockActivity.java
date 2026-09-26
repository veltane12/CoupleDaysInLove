package com.example.coupledaysinlove;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class SecurityLockActivity extends AppCompatActivity {

    public static boolean isAppUnlocked = false;

    private PreferencesHelper prefs;
    private StringBuilder currentInput = new StringBuilder();

    private TextView tvErrorMsg;
    private View layoutDots;
    private ImageView dot1, dot2, dot3, dot4;
    private MaterialButton btnKeyBiometric;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public static void start(Context context) {
        Intent intent = new Intent(context, SecurityLockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PreferencesHelper(this);
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

        setContentView(R.layout.activity_security_lock);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(!isDark);
        windowInsetsController.setAppearanceLightNavigationBars(!isDark);

        View mainView = findViewById(R.id.layoutLockContainer);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                WindowInsetsCompat bars = insets;
                v.setPadding(
                        bars.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                        bars.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                        bars.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                        bars.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                );
                return insets;
            });
        }

        tvErrorMsg = findViewById(R.id.tvErrorMsg);
        layoutDots = findViewById(R.id.layoutDots);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        btnKeyBiometric = findViewById(R.id.btnKeyBiometric);

        setupKeypad();
        setupBiometrics();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        if (isBiometricAvailable() && prefs.isBiometricEnabled()) {
            btnKeyBiometric.setVisibility(View.VISIBLE);
            triggerBiometricPrompt();
        } else {
            btnKeyBiometric.setVisibility(View.INVISIBLE);
        }
    }

    private void setupKeypad() {
        int[] buttonIds = {
                R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
                R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final String digit = String.valueOf(i);
            MaterialButton btn = findViewById(buttonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> onDigitPressed(digit));
            }
        }

        MaterialButton btnDelete = findViewById(R.id.btnKeyDelete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> onDeletePressed());
        }

        if (btnKeyBiometric != null) {
            btnKeyBiometric.setOnClickListener(v -> triggerBiometricPrompt());
        }
    }

    private void setupBiometrics() {
        if (!isBiometricAvailable()) return;

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                unlockApp();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_prompt_negative))
                .build();
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void triggerBiometricPrompt() {
        if (biometricPrompt != null && promptInfo != null) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void onDigitPressed(String digit) {
        if (currentInput.length() < 4) {
            currentInput.append(digit);
            updateDotsUI();
            tvErrorMsg.setVisibility(View.INVISIBLE);

            if (currentInput.length() == 4) {
                checkPin(currentInput.toString());
            }
        }
    }

    private void onDeletePressed() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDotsUI();
            tvErrorMsg.setVisibility(View.INVISIBLE);
        }
    }

    private void checkPin(String enteredPin) {
        String savedPin = prefs.getSecurityPin();
        if (savedPin != null && savedPin.equals(enteredPin)) {
            unlockApp();
        } else {
            showError(getString(R.string.wrong_pin));
        }
    }

    private void showError(String message) {
        tvErrorMsg.setText(message);
        tvErrorMsg.setVisibility(View.VISIBLE);
        currentInput.setLength(0);
        updateDotsUI();

        vibrateError();

        if (layoutDots != null) {
            Animation shake = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            layoutDots.startAnimation(shake);
        }
    }

    private void vibrateError() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Exception ignored) {
        }
    }

    private void updateDotsUI() {
        int length = currentInput.length();
        dot1.setImageResource(length >= 1 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot2.setImageResource(length >= 2 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot3.setImageResource(length >= 3 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot4.setImageResource(length >= 4 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
    }

    private void unlockApp() {
        isAppUnlocked = true;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.setLastAuthDate(today);
        setResult(RESULT_OK);
        finish();
    }
}