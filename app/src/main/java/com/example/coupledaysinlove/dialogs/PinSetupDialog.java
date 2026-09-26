package com.example.coupledaysinlove.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.google.android.material.button.MaterialButton;

public class PinSetupDialog extends Dialog {

    public static final int MODE_SET_PIN = 1;
    public static final int MODE_VERIFY_PIN = 2;
    public static final int MODE_CHANGE_PIN = 3;

    private static final int STEP_VERIFY_OLD = 10;
    private static final int STEP_ENTER_NEW = 11;
    private static final int STEP_CONFIRM_NEW = 12;

    public interface PinSetupListener {
        void onPinSetSuccessfully(String newPin);
        void onPinVerifiedSuccessfully();
        void onPinSetupCanceled();
    }

    private final int mode;
    private final PinSetupListener listener;
    private final PreferencesHelper prefsHelper;

    private int currentStep;
    private String tempNewPin = "";
    private StringBuilder currentInput = new StringBuilder();

    private TextView tvTitle;
    private TextView tvInstruction;
    private TextView tvErrorMsg;
    private View layoutDots;
    private ImageView dot1, dot2, dot3, dot4;

    public PinSetupDialog(@NonNull Context context, int mode, PinSetupListener listener) {
        super(context);
        this.mode = mode;
        this.listener = listener;
        this.prefsHelper = new PreferencesHelper(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pin_setup);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvTitle = findViewById(R.id.tvDialogTitle);
        tvInstruction = findViewById(R.id.tvDialogInstruction);
        tvErrorMsg = findViewById(R.id.tvDialogErrorMsg);
        layoutDots = findViewById(R.id.layoutDialogDots);

        dot1 = findViewById(R.id.dialogDot1);
        dot2 = findViewById(R.id.dialogDot2);
        dot3 = findViewById(R.id.dialogDot3);
        dot4 = findViewById(R.id.dialogDot4);

        setupKeypad();

        if (mode == MODE_SET_PIN) {
            currentStep = STEP_ENTER_NEW;
        } else if (mode == MODE_VERIFY_PIN || mode == MODE_CHANGE_PIN) {
            currentStep = STEP_VERIFY_OLD;
        }

        updateStepUI();
    }

    private void setupKeypad() {
        int[] buttonIds = {
                R.id.btnDlgKey0, R.id.btnDlgKey1, R.id.btnDlgKey2, R.id.btnDlgKey3, R.id.btnDlgKey4,
                R.id.btnDlgKey5, R.id.btnDlgKey6, R.id.btnDlgKey7, R.id.btnDlgKey8, R.id.btnDlgKey9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final String digit = String.valueOf(i);
            MaterialButton btn = findViewById(buttonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> onDigitPressed(digit));
            }
        }

        MaterialButton btnDelete = findViewById(R.id.btnDlgKeyDelete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> onDeletePressed());
        }

        MaterialButton btnCancel = findViewById(R.id.btnDlgCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onPinSetupCanceled();
                dismiss();
            });
        }
    }

    private void onDigitPressed(String digit) {
        if (currentInput.length() < 4) {
            currentInput.append(digit);
            updateDotsUI();
            tvErrorMsg.setVisibility(View.INVISIBLE);

            if (currentInput.length() == 4) {
                onFourDigitsEntered(currentInput.toString());
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

    private void onFourDigitsEntered(String enteredPin) {
        if (currentStep == STEP_VERIFY_OLD) {
            String savedPin = prefsHelper.getSecurityPin();
            if (savedPin != null && savedPin.equals(enteredPin)) {
                if (mode == MODE_VERIFY_PIN) {
                    if (listener != null) listener.onPinVerifiedSuccessfully();
                    dismiss();
                } else if (mode == MODE_CHANGE_PIN) {
                    currentStep = STEP_ENTER_NEW;
                    currentInput.setLength(0);
                    updateDotsUI();
                    updateStepUI();
                }
            } else {
                showError(getContext().getString(R.string.wrong_pin));
            }
        } else if (currentStep == STEP_ENTER_NEW) {
            tempNewPin = enteredPin;
            currentStep = STEP_CONFIRM_NEW;
            currentInput.setLength(0);
            updateDotsUI();
            updateStepUI();
        } else if (currentStep == STEP_CONFIRM_NEW) {
            if (tempNewPin.equals(enteredPin)) {
                prefsHelper.setSecurityPin(enteredPin);
                prefsHelper.setSecurityLockEnabled(true);
                if (listener != null) listener.onPinSetSuccessfully(enteredPin);
                dismiss();
            } else {
                showError(getContext().getString(R.string.pin_dont_match));
                currentStep = STEP_ENTER_NEW;
                tempNewPin = "";
                currentInput.setLength(0);
                updateDotsUI();
                updateStepUI();
            }
        }
    }

    private void showError(String message) {
        tvErrorMsg.setText(message);
        tvErrorMsg.setVisibility(View.VISIBLE);
        currentInput.setLength(0);
        updateDotsUI();

        if (layoutDots != null) {
            Animation shake = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
            layoutDots.startAnimation(shake);
        }
    }

    private void updateStepUI() {
        tvErrorMsg.setVisibility(View.INVISIBLE);

        if (currentStep == STEP_VERIFY_OLD) {
            tvTitle.setText(R.string.enter_current_pin_title);
            tvInstruction.setText(R.string.enter_current_pin_instruction);
        } else if (currentStep == STEP_ENTER_NEW) {
            tvTitle.setText(mode == MODE_CHANGE_PIN ? R.string.change_pin_title : R.string.set_pin_title);
            tvInstruction.setText(R.string.set_pin_instruction);
        } else if (currentStep == STEP_CONFIRM_NEW) {
            tvTitle.setText(mode == MODE_CHANGE_PIN ? R.string.change_pin_title : R.string.set_pin_title);
            tvInstruction.setText(R.string.confirm_pin_instruction);
        }
    }

    private void updateDotsUI() {
        int length = currentInput.length();
        dot1.setImageResource(length >= 1 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot2.setImageResource(length >= 2 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot3.setImageResource(length >= 3 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
        dot4.setImageResource(length >= 4 ? R.drawable.bg_pin_dot_on : R.drawable.bg_pin_dot_off);
    }
}