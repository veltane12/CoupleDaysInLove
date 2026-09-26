package com.example.coupledaysinlove.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.dialogs.CropImageDialog;
import com.example.coupledaysinlove.dialogs.SelectDateDialog;
import com.example.coupledaysinlove.helpers.DateUtils;
import com.example.coupledaysinlove.helpers.ImageLoaderHelper;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.example.coupledaysinlove.widgets.CoupleWidgetProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private ImageView ivSettingsPhoto1;
    private ImageView ivSettingsPhoto2;
    private TextInputEditText etPartner1;
    private TextInputEditText etPartner2;
    private TextView tvSettingsStartDate;
    private MaterialButton btnDeleteStartDate;

    private PreferencesHelper prefsHelper;

    private ActivityResultLauncher<String> pickerPhoto1;
    private ActivityResultLauncher<String> pickerPhoto2;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickerPhoto1 = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                CropImageDialog.show(requireContext(), uri, "partner1_photo.jpg", path -> {
                    ImageLoaderHelper.clearCacheForPath(path);
                    prefsHelper.setPartner1PhotoPath(path);
                    ImageLoaderHelper.loadPhotoInto(requireContext(), path, ivSettingsPhoto1);
                    Toast.makeText(requireContext(), R.string.photo_updated, Toast.LENGTH_SHORT).show();
                    CoupleWidgetProvider.updateAllWidgets(requireContext());
                });
            }
        });

        pickerPhoto2 = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                CropImageDialog.show(requireContext(), uri, "partner2_photo.jpg", path -> {
                    ImageLoaderHelper.clearCacheForPath(path);
                    prefsHelper.setPartner2PhotoPath(path);
                    ImageLoaderHelper.loadPhotoInto(requireContext(), path, ivSettingsPhoto2);
                    Toast.makeText(requireContext(), R.string.photo_updated, Toast.LENGTH_SHORT).show();
                    CoupleWidgetProvider.updateAllWidgets(requireContext());
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new PreferencesHelper(requireContext());

        ivSettingsPhoto1 = view.findViewById(R.id.ivSettingsPhoto1);
        ivSettingsPhoto2 = view.findViewById(R.id.ivSettingsPhoto2);
        View cardPhoto1 = view.findViewById(R.id.cardPhotoContainer1);
        View cardPhoto2 = view.findViewById(R.id.cardPhotoContainer2);
        MaterialButton btnChangePhoto1 = view.findViewById(R.id.btnChangePhoto1);
        MaterialButton btnChangePhoto2 = view.findViewById(R.id.btnChangePhoto2);
        MaterialButton btnDeletePhoto1 = view.findViewById(R.id.btnDeletePhoto1);
        MaterialButton btnDeletePhoto2 = view.findViewById(R.id.btnDeletePhoto2);
        etPartner1 = view.findViewById(R.id.etPartner1);
        etPartner2 = view.findViewById(R.id.etPartner2);
        MaterialButton btnSaveNames = view.findViewById(R.id.btnSaveNames);
        tvSettingsStartDate = view.findViewById(R.id.tvSettingsStartDate);
        MaterialButton btnChangeStartDate = view.findViewById(R.id.btnChangeStartDate);
        btnDeleteStartDate = view.findViewById(R.id.btnDeleteStartDate);

        loadValues();

        if (cardPhoto1 != null) cardPhoto1.setOnClickListener(v -> pickerPhoto1.launch("image/*"));
        if (cardPhoto2 != null) cardPhoto2.setOnClickListener(v -> pickerPhoto2.launch("image/*"));

        btnChangePhoto1.setOnClickListener(v -> pickerPhoto1.launch("image/*"));
        btnChangePhoto2.setOnClickListener(v -> pickerPhoto2.launch("image/*"));

        btnDeletePhoto1.setOnClickListener(v -> {
            if (prefsHelper.getPartner1PhotoPath() != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_photo_confirm_title)
                        .setMessage(R.string.delete_photo_confirm_msg)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            ImageLoaderHelper.clearCacheForPath(prefsHelper.getPartner1PhotoPath());
                            prefsHelper.setPartner1PhotoPath(null);
                            ImageLoaderHelper.loadPhotoInto(requireContext(), null, ivSettingsPhoto1);
                            Toast.makeText(requireContext(), R.string.photo_deleted, Toast.LENGTH_SHORT).show();
                            CoupleWidgetProvider.updateAllWidgets(requireContext());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        btnDeletePhoto2.setOnClickListener(v -> {
            if (prefsHelper.getPartner2PhotoPath() != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_photo_confirm_title)
                        .setMessage(R.string.delete_photo_confirm_msg)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            ImageLoaderHelper.clearCacheForPath(prefsHelper.getPartner2PhotoPath());
                            prefsHelper.setPartner2PhotoPath(null);
                            ImageLoaderHelper.loadPhotoInto(requireContext(), null, ivSettingsPhoto2);
                            Toast.makeText(requireContext(), R.string.photo_deleted, Toast.LENGTH_SHORT).show();
                            CoupleWidgetProvider.updateAllWidgets(requireContext());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        btnSaveNames.setOnClickListener(v -> {
            String p1 = etPartner1.getText() != null ? etPartner1.getText().toString().trim() : "";
            String p2 = etPartner2.getText() != null ? etPartner2.getText().toString().trim() : "";

            if (!p1.isEmpty()) prefsHelper.setPartner1Name(p1);
            if (!p2.isEmpty()) prefsHelper.setPartner2Name(p2);

            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
            CoupleWidgetProvider.updateAllWidgets(requireContext());
        });

        btnChangeStartDate.setOnClickListener(v -> showDateDialog());
        tvSettingsStartDate.setOnClickListener(v -> showDateDialog());

        btnDeleteStartDate.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_anniversary_confirm_title)
                    .setMessage(R.string.delete_anniversary_confirm_msg)
                    .setPositiveButton(R.string.clear_anniversary_date, (dialog, which) -> {
                        prefsHelper.clearStartDate();
                        loadValues();
                        Toast.makeText(requireContext(), R.string.anniversary_cleared, Toast.LENGTH_SHORT).show();
                        CoupleWidgetProvider.updateAllWidgets(requireContext());
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadValues();
    }

    private void loadValues() {
        etPartner1.setText(prefsHelper.getPartner1Name());
        etPartner2.setText(prefsHelper.getPartner2Name());

        ImageLoaderHelper.loadPhotoInto(requireContext(), prefsHelper.getPartner1PhotoPath(), ivSettingsPhoto1);
        ImageLoaderHelper.loadPhotoInto(requireContext(), prefsHelper.getPartner2PhotoPath(), ivSettingsPhoto2);

        if (prefsHelper.hasStartDate()) {
            long startMillis = prefsHelper.getStartDateMillis();
            tvSettingsStartDate.setText(DateUtils.getFormattedDate(startMillis));
            btnDeleteStartDate.setVisibility(View.VISIBLE);
        } else {
            tvSettingsStartDate.setText(R.string.no_date_selected);
            btnDeleteStartDate.setVisibility(View.GONE);
        }

        CoupleWidgetProvider.updateAllWidgets(requireContext());
    }

    private void showDateDialog() {
        long currentMillis = prefsHelper.hasStartDate() ? prefsHelper.getStartDateMillis() : -1;
        SelectDateDialog.show(requireContext(), currentMillis, selectedMillis -> {
            prefsHelper.setStartDateMillis(selectedMillis);
            loadValues();
            CoupleWidgetProvider.updateAllWidgets(requireContext());
        });
    }
}