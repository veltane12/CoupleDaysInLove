package com.example.coupledaysinlove.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.adapters.BucketAdapter;
import com.example.coupledaysinlove.dialogs.AddBucketItemDialog;
import com.example.coupledaysinlove.dialogs.SelectDateDialog;
import com.example.coupledaysinlove.helpers.DateUtils;
import com.example.coupledaysinlove.helpers.ImageLoaderHelper;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.example.coupledaysinlove.helpers.ShareCardHelper;
import com.example.coupledaysinlove.models.BucketItem;
import com.example.coupledaysinlove.widgets.CoupleWidgetProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CounterFragment extends Fragment {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_PENDING = 1;
    private static final int FILTER_COMPLETED = 2;

    private ImageView ivPartner1Photo;
    private ImageView ivPartner2Photo;
    private TextView tvPartner1Name;
    private TextView tvPartner2Name;
    private TextView tvStartDate;
    private TextView tvDaysCount;
    private TextView tvDetailedDuration;
    private TextView tvNextAnniversaryCountdown;
    private MaterialButton btnSelectDate;
    private MaterialButton btnShareAchievement;

    // Bucket List UI Components
    private TextView tvBucketProgress;
    private LinearProgressIndicator progressBucket;
    private Chip chipFilterAll;
    private Chip chipFilterPending;
    private Chip chipFilterCompleted;
    private RecyclerView rvBucketList;
    private LinearLayout layoutEmptyBucket;
    private MaterialButton btnAddBucketItem;

    private PreferencesHelper prefsHelper;
    private BucketAdapter bucketAdapter;
    private int currentFilterMode = FILTER_ALL;

    public CounterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_counter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new PreferencesHelper(requireContext());

        ivPartner1Photo = view.findViewById(R.id.ivPartner1Photo);
        ivPartner2Photo = view.findViewById(R.id.ivPartner2Photo);
        tvPartner1Name = view.findViewById(R.id.tvPartner1Name);
        tvPartner2Name = view.findViewById(R.id.tvPartner2Name);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvDaysCount = view.findViewById(R.id.tvDaysCount);
        tvDetailedDuration = view.findViewById(R.id.tvDetailedDuration);
        tvNextAnniversaryCountdown = view.findViewById(R.id.tvNextAnniversaryCountdown);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        btnShareAchievement = view.findViewById(R.id.btnShareAchievement);

        // Bucket List Views
        tvBucketProgress = view.findViewById(R.id.tvBucketProgress);
        progressBucket = view.findViewById(R.id.progressBucket);
        chipFilterAll = view.findViewById(R.id.chipFilterAll);
        chipFilterPending = view.findViewById(R.id.chipFilterPending);
        chipFilterCompleted = view.findViewById(R.id.chipFilterCompleted);
        rvBucketList = view.findViewById(R.id.rvBucketList);
        layoutEmptyBucket = view.findViewById(R.id.layoutEmptyBucket);
        btnAddBucketItem = view.findViewById(R.id.btnAddBucketItem);

        View cardNextAnniversary = view.findViewById(R.id.cardNextAnniversary);
        if (cardNextAnniversary != null) {
            cardNextAnniversary.setOnClickListener(v -> showDateDialog());
        }

        btnSelectDate.setOnClickListener(v -> showDateDialog());
        tvStartDate.setOnClickListener(v -> showDateDialog());
        btnShareAchievement.setOnClickListener(v -> ShareCardHelper.generateAndShareCard(requireContext(), prefsHelper));

        setupBucketListUI();

        updateUI();
    }

    private void setupBucketListUI() {
        bucketAdapter = new BucketAdapter(
                (item, isChecked) -> {
                    long completionDate = isChecked ? System.currentTimeMillis() : 0L;
                    prefsHelper.toggleBucketItemCompleted(item.getId(), isChecked, completionDate);
                    if (isChecked) {
                        Toast.makeText(requireContext(), R.string.goal_completed_toast, Toast.LENGTH_SHORT).show();
                    }
                    loadBucketList();
                },
                itemToEdit -> {
                    AddBucketItemDialog.show(requireContext(), itemToEdit, updatedItem -> {
                        prefsHelper.saveBucketItem(updatedItem);
                        loadBucketList();
                    });
                },
                itemToDelete -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete_bucket_confirm_title)
                            .setMessage(R.string.delete_bucket_confirm_msg)
                            .setPositiveButton(R.string.delete, (dialog, which) -> {
                                prefsHelper.deleteBucketItem(itemToDelete.getId());
                                Toast.makeText(requireContext(), R.string.bucket_item_deleted, Toast.LENGTH_SHORT).show();
                                loadBucketList();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
        );

        rvBucketList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBucketList.setAdapter(bucketAdapter);

        btnAddBucketItem.setOnClickListener(v -> {
            AddBucketItemDialog.show(requireContext(), newItem -> {
                prefsHelper.saveBucketItem(newItem);
                loadBucketList();
            });
        });

        chipFilterAll.setOnClickListener(v -> {
            currentFilterMode = FILTER_ALL;
            loadBucketList();
        });

        chipFilterPending.setOnClickListener(v -> {
            currentFilterMode = FILTER_PENDING;
            loadBucketList();
        });

        chipFilterCompleted.setOnClickListener(v -> {
            currentFilterMode = FILTER_COMPLETED;
            loadBucketList();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        if (!isAdded() || getContext() == null) return;

        String p1 = prefsHelper.getPartner1Name();
        String p2 = prefsHelper.getPartner2Name();
        tvPartner1Name.setText(p1);
        tvPartner2Name.setText(p2);

        // Load Photos using async memory-cached ImageLoaderHelper
        ImageLoaderHelper.loadPhotoInto(requireContext(), prefsHelper.getPartner1PhotoPath(), ivPartner1Photo);
        ImageLoaderHelper.loadPhotoInto(requireContext(), prefsHelper.getPartner2PhotoPath(), ivPartner2Photo);

        if (prefsHelper.hasStartDate()) {
            btnSelectDate.setVisibility(View.GONE);

            long startMillis = prefsHelper.getStartDateMillis();
            String formattedDate = DateUtils.getFormattedDate(startMillis);
            tvStartDate.setText(getString(R.string.since_date_prefix) + " " + formattedDate);

            long daysTogether = DateUtils.getDaysTogether(startMillis);
            tvDaysCount.setText(String.valueOf(daysTogether));

            String detailed = DateUtils.getDetailedDuration(startMillis);
            tvDetailedDuration.setText(detailed);

            long daysToAnniversary = DateUtils.getDaysUntilNextOccurrence(startMillis);
            if (daysToAnniversary == 0) {
                tvNextAnniversaryCountdown.setText("¡Hoy es su Aniversario! 🎉❤️");
            } else {
                tvNextAnniversaryCountdown.setText("Faltan " + daysToAnniversary + " días");
            }
        } else {
            btnSelectDate.setVisibility(View.VISIBLE);

            tvStartDate.setText(R.string.no_start_date_msg);
            tvDaysCount.setText("0");
            tvDetailedDuration.setText("¡Comiencen su historia juntos!");
            tvNextAnniversaryCountdown.setText("Selecciona fecha de inicio");
        }

        loadBucketList();

        CoupleWidgetProvider.updateAllWidgets(requireContext());
    }

    private void loadBucketList() {
        if (!isAdded() || getContext() == null) return;

        List<BucketItem> allItems = prefsHelper.getBucketItems();
        int totalCount = allItems.size();
        int completedCount = 0;

        for (BucketItem item : allItems) {
            if (item.isCompleted()) {
                completedCount++;
            }
        }

        int percentage = totalCount > 0 ? (int) Math.round((completedCount * 100.0) / totalCount) : 0;
        progressBucket.setProgress(percentage);
        tvBucketProgress.setText(getString(R.string.bucket_list_progress, completedCount, totalCount, percentage));

        List<BucketItem> filteredItems = new ArrayList<>();
        for (BucketItem item : allItems) {
            if (currentFilterMode == FILTER_PENDING && !item.isCompleted()) {
                filteredItems.add(item);
            } else if (currentFilterMode == FILTER_COMPLETED && item.isCompleted()) {
                filteredItems.add(item);
            } else if (currentFilterMode == FILTER_ALL) {
                filteredItems.add(item);
            }
        }

        if (filteredItems.isEmpty()) {
            layoutEmptyBucket.setVisibility(View.VISIBLE);
            rvBucketList.setVisibility(View.GONE);
        } else {
            layoutEmptyBucket.setVisibility(View.GONE);
            rvBucketList.setVisibility(View.VISIBLE);
            bucketAdapter.setBucketItems(filteredItems);
        }
    }

    private void showDateDialog() {
        long currentMillis = prefsHelper.hasStartDate() ? prefsHelper.getStartDateMillis() : -1;
        SelectDateDialog.show(requireContext(), currentMillis, selectedMillis -> {
            prefsHelper.setStartDateMillis(selectedMillis);
            updateUI();
        });
    }
}