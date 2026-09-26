package com.example.coupledaysinlove.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.models.BucketItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddBucketItemDialog {

    public interface OnBucketSavedListener {
        void onSaved(BucketItem item);
    }

    public static void show(Context context, OnBucketSavedListener listener) {
        show(context, null, listener);
    }

    public static void show(Context context, BucketItem itemToEdit, OnBucketSavedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_bucket_item, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvBucketDialogTitle);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etBucketTitle);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerBucketCategory);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelBucket);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveBucket);

        if (itemToEdit != null) {
            tvTitle.setText(R.string.edit_bucket_item);
        } else {
            tvTitle.setText(R.string.add_bucket_item);
        }

        String[] categories = {"Citas", "Viajes", "Aventura", "Gastronomía", "Hogar", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        if (itemToEdit != null) {
            etTitle.setText(itemToEdit.getTitle());
            spinnerCategory.setText(itemToEdit.getCategory(), false);
        } else {
            spinnerCategory.setText(categories[0], false);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "Otro";

            if (title.isEmpty()) {
                etTitle.setError("Ingresa el título de la meta");
                return;
            }

            String id = (itemToEdit != null) ? itemToEdit.getId() : String.valueOf(System.currentTimeMillis());
            boolean completed = (itemToEdit != null) && itemToEdit.isCompleted();
            long completedDateMillis = (itemToEdit != null) ? itemToEdit.getCompletedDateMillis() : 0L;
            long createdDateMillis = (itemToEdit != null) ? itemToEdit.getCreatedDateMillis() : System.currentTimeMillis();

            BucketItem item = new BucketItem(id, title, category, completed, completedDateMillis, createdDateMillis);

            if (listener != null) {
                listener.onSaved(item);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}