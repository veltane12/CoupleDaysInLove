package com.example.coupledaysinlove.dialogs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.helpers.ImageLoaderHelper;
import com.example.coupledaysinlove.models.SpecialEvent;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class AddEventDialog {

    public interface OnEventSavedListener {
        void onSaved(SpecialEvent event);
    }

    public interface OnSelectPhotoListener {
        void onSelectPhoto();
    }

    private final Context context;
    private final SpecialEvent eventToEdit;
    private final OnSelectPhotoListener photoListener;
    private final OnEventSavedListener saveListener;

    private AlertDialog dialog;
    private ImageView ivEventPhotoPreview;
    private View layoutAddPhotoPrompt;
    private MaterialButton btnRemovePhoto;
    private String currentPhotoPath;

    public AddEventDialog(Context context, SpecialEvent eventToEdit, OnSelectPhotoListener photoListener, OnEventSavedListener saveListener) {
        this.context = context;
        this.eventToEdit = eventToEdit;
        this.photoListener = photoListener;
        this.saveListener = saveListener;
        if (eventToEdit != null) {
            this.currentPhotoPath = eventToEdit.getPhotoPath();
        }
    }

    public static AddEventDialog show(Context context, OnEventSavedListener listener) {
        return show(context, null, null, listener);
    }

    public static AddEventDialog show(Context context, SpecialEvent eventToEdit, OnEventSavedListener listener) {
        return show(context, eventToEdit, null, listener);
    }

    public static AddEventDialog show(Context context, SpecialEvent eventToEdit, OnSelectPhotoListener photoListener, OnEventSavedListener saveListener) {
        AddEventDialog addEventDialog = new AddEventDialog(context, eventToEdit, photoListener, saveListener);
        addEventDialog.showInternal();
        return addEventDialog;
    }

    public void setPhotoPath(String path) {
        this.currentPhotoPath = path;
        updatePhotoUi();
    }

    private void updatePhotoUi() {
        if (ivEventPhotoPreview == null) return;

        if (currentPhotoPath != null && !currentPhotoPath.isEmpty() && new File(currentPhotoPath).exists()) {
            ivEventPhotoPreview.setVisibility(View.VISIBLE);
            if (layoutAddPhotoPrompt != null) layoutAddPhotoPrompt.setVisibility(View.GONE);
            if (btnRemovePhoto != null) btnRemovePhoto.setVisibility(View.VISIBLE);
            ImageLoaderHelper.loadEventPhotoInto(context, currentPhotoPath, ivEventPhotoPreview);
        } else {
            ivEventPhotoPreview.setVisibility(View.GONE);
            if (layoutAddPhotoPrompt != null) layoutAddPhotoPrompt.setVisibility(View.VISIBLE);
            if (btnRemovePhoto != null) btnRemovePhoto.setVisibility(View.GONE);
        }
    }

    private void showInternal() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etEventTitle);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        TextInputLayout tilDate = dialogView.findViewById(R.id.tilEventDate);
        TextInputEditText etDate = dialogView.findViewById(R.id.etEventDate);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etEventDescription);

        MaterialCardView cardPhotoPicker = dialogView.findViewById(R.id.cardPhotoPicker);
        layoutAddPhotoPrompt = dialogView.findViewById(R.id.layoutAddPhotoPrompt);
        ivEventPhotoPreview = dialogView.findViewById(R.id.ivEventPhotoPreview);
        btnRemovePhoto = dialogView.findViewById(R.id.btnRemovePhoto);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelEvent);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveEvent);

        if (eventToEdit != null) {
            tvTitle.setText(R.string.edit_event);
        } else {
            tvTitle.setText(R.string.add_event);
        }

        String[] categories = {"Cumpleaños", "Aniversario", "Cita Especial", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        final Calendar selectedCal = Calendar.getInstance();

        Runnable updateDateHint = () -> {
            String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString() : "";
            if ("Cumpleaños".equalsIgnoreCase(category)) {
                tilDate.setHint(context.getString(R.string.birth_date_hint) + " (DD/MM/AA)");
            } else {
                tilDate.setHint(context.getString(R.string.event_date_hint) + " (DD/MM/AA)");
            }
        };

        if (eventToEdit != null) {
            etTitle.setText(eventToEdit.getTitle());
            spinnerCategory.setText(eventToEdit.getCategory(), false);
            selectedCal.setTimeInMillis(eventToEdit.getDateMillis());
            etDate.setText(formatCalToDDMMAA(selectedCal));
            if (eventToEdit.getDescription() != null) {
                etDescription.setText(eventToEdit.getDescription());
            }
        } else {
            spinnerCategory.setText(categories[0], false);
            etDate.setText("");
        }

        updatePhotoUi();

        if (cardPhotoPicker != null) {
            cardPhotoPicker.setOnClickListener(v -> {
                if (photoListener != null) {
                    photoListener.onSelectPhoto();
                }
            });
        }

        if (btnRemovePhoto != null) {
            btnRemovePhoto.setOnClickListener(v -> setPhotoPath(null));
        }

        updateDateHint.run();
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> updateDateHint.run());

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
                    if (digits.length() > 0) {
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

        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "Otro";
            String dateText = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            String description = etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : null;

            tilDate.setError(null);
            etTitle.setError(null);

            if (title.isEmpty()) {
                etTitle.setError("Ingresa el título del evento");
                return;
            }

            if (dateText.isEmpty()) {
                tilDate.setError("Ingresa la fecha del evento");
                return;
            }

            long parsedMillis = parseDateTextDDMMAA(dateText);
            if (parsedMillis <= 0) {
                tilDate.setError("Fecha inválida (DD/MM/AA)");
                return;
            }

            selectedCal.setTimeInMillis(parsedMillis);

            String id = (eventToEdit != null) ? eventToEdit.getId() : String.valueOf(System.currentTimeMillis());
            boolean repeatYearly = "Cumpleaños".equalsIgnoreCase(category) || "Aniversario".equalsIgnoreCase(category);

            SpecialEvent savedEvent = new SpecialEvent(id, title, selectedCal.getTimeInMillis(), category, repeatYearly, currentPhotoPath, description);

            if (saveListener != null) {
                saveListener.onSaved(savedEvent);
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