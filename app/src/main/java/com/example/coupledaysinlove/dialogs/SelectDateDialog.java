package com.example.coupledaysinlove.dialogs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.coupledaysinlove.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class SelectDateDialog {

    public interface OnDateSelectedListener {
        void onDateSelected(long dateMillis);
    }

    public static void show(Context context, long initialMillis, OnDateSelectedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_date, null);

        TextInputLayout tilDate = dialogView.findViewById(R.id.tilSelectDate);
        TextInputEditText etDate = dialogView.findViewById(R.id.etSelectDate);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelDate);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveDate);

        final Calendar selectedCal = Calendar.getInstance();
        if (initialMillis > 0) {
            selectedCal.setTimeInMillis(initialMillis);
            etDate.setText(formatCalToDDMMAA(selectedCal));
        } else {
            etDate.setText("");
        }

        // Automatic Date Separator TextWatcher (DD/MM/AA)
        etDate.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            private boolean isDeleting = false;
            private char deletedChar = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
                if (isDeleting && count == 1 && start < s.length()) {
                    deletedChar = s.charAt(start);
                } else {
                    deletedChar = 0;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDate.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String raw = s.toString();
                String digits = raw.replaceAll("\\D", "");

                // Handle backspacing at slash boundary cleanly
                if (isDeleting && deletedChar == '/') {
                    if (!digits.isEmpty()) {
                        digits = digits.substring(0, digits.length() - 1);
                    }
                }

                if (digits.length() > 8) {
                    digits = digits.substring(0, 8);
                }

                StringBuilder formatted = new StringBuilder();
                int len = digits.length();
                for (int i = 0; i < len; i++) {
                    formatted.append(digits.charAt(i));
                    // Insert slash after 2nd digit (DD) and 4th digit (MM)
                    if (i == 1 || i == 3) {
                        if (i < len - 1 || !isDeleting) {
                            formatted.append("/");
                        }
                    }
                }

                s.replace(0, s.length(), formatted.toString());

                if (etDate.getText() != null) {
                    etDate.setSelection(etDate.getText().length());
                }
                isEditing = false;
            }
        });

        View.OnClickListener openDatePicker = v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        selectedCal.set(Calendar.YEAR, year);
                        selectedCal.set(Calendar.MONTH, month);
                        selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCal.set(Calendar.MINUTE, 0);
                        selectedCal.set(Calendar.SECOND, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);

                        etDate.setText(formatCalToDDMMAA(selectedCal));
                    },
                    selectedCal.get(Calendar.YEAR),
                    selectedCal.get(Calendar.MONTH),
                    selectedCal.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        };

        tilDate.setEndIconOnClickListener(openDatePicker);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String dateText = etDate.getText() != null ? etDate.getText().toString().trim() : "";

            tilDate.setError(null);

            if (dateText.isEmpty()) {
                tilDate.setError(context.getString(R.string.enter_date_msg));
                return;
            }

            long parsedMillis = parseDateTextDDMMAA(dateText);
            if (parsedMillis <= 0) {
                tilDate.setError(context.getString(R.string.invalid_date_msg));
                return;
            }

            if (listener != null) {
                listener.onDateSelected(parsedMillis);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private static String formatCalToDDMMAA(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR) % 100;
        return String.format(Locale.getDefault(), "%02d/%02d/%02d", day, month, year);
    }

    private static long parseDateTextDDMMAA(String dateText) {
        if (dateText == null) return -1;
        String digits = dateText.replaceAll("\\D", "");
        if (digits.length() < 6) return -1;

        try {
            int day = Integer.parseInt(digits.substring(0, 2));
            int month = Integer.parseInt(digits.substring(2, 4)) - 1; // 0-based month

            int fullYear;
            if (digits.length() >= 8) {
                fullYear = Integer.parseInt(digits.substring(4, 8));
            } else {
                int shortYear = Integer.parseInt(digits.substring(4, 6));
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int currentTwoDigits = currentYear % 100;
                if (shortYear > currentTwoDigits) {
                    fullYear = 1900 + shortYear;
                } else {
                    fullYear = 2000 + shortYear;
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.setLenient(false);
            cal.set(Calendar.YEAR, fullYear);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }
}