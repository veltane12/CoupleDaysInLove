package com.example.coupledaysinlove.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.adapters.EventAdapter;
import com.example.coupledaysinlove.dialogs.AddEventDialog;
import com.example.coupledaysinlove.dialogs.CropImageDialog;
import com.example.coupledaysinlove.helpers.ImageLoaderHelper;
import com.example.coupledaysinlove.helpers.NotificationHelper;
import com.example.coupledaysinlove.helpers.PreferencesHelper;
import com.example.coupledaysinlove.models.SpecialEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class EventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private LinearLayout layoutEmptyEvents;

    private PreferencesHelper prefsHelper;
    private EventAdapter adapter;

    private ActivityResultLauncher<String> selectPhotoLauncher;
    private AddEventDialog activeAddEventDialog;

    public EventsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String outputFileName = "event_photo_" + System.currentTimeMillis() + ".jpg";
                CropImageDialog.show(requireContext(), uri, outputFileName, 280, 160, savedPath -> {
                    ImageLoaderHelper.clearCacheForPath(savedPath);
                    if (activeAddEventDialog != null) {
                        activeAddEventDialog.setPhotoPath(savedPath);
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new PreferencesHelper(requireContext());

        rvEvents = view.findViewById(R.id.rvEvents);
        layoutEmptyEvents = view.findViewById(R.id.layoutEmptyEvents);
        ExtendedFloatingActionButton fabAddEvent = view.findViewById(R.id.fabAddEvent);

        adapter = new EventAdapter(
                eventToDelete -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete_event_confirm_title)
                            .setMessage(R.string.delete_event_confirm_msg)
                            .setPositiveButton(R.string.delete, (dialog, which) -> {
                                prefsHelper.deleteEvent(eventToDelete.getId());
                                Toast.makeText(requireContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                                loadEvents();
                                NotificationHelper.scheduleAllNotifications(requireContext());
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                },
                eventToEdit -> {
                    activeAddEventDialog = AddEventDialog.show(
                            requireContext(),
                            eventToEdit,
                            () -> selectPhotoLauncher.launch("image/*"),
                            editedEvent -> {
                                prefsHelper.saveEvent(editedEvent);
                                loadEvents();
                                NotificationHelper.scheduleAllNotifications(requireContext());
                            }
                    );
                }
        );

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvents.setAdapter(adapter);

        fabAddEvent.setOnClickListener(v -> {
            activeAddEventDialog = AddEventDialog.show(
                    requireContext(),
                    null,
                    () -> selectPhotoLauncher.launch("image/*"),
                    newEvent -> {
                        prefsHelper.saveEvent(newEvent);
                        loadEvents();
                        NotificationHelper.scheduleAllNotifications(requireContext());
                    }
            );
        });

        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        if (!isAdded() || getContext() == null) return;

        List<SpecialEvent> list = prefsHelper.getEvents();
        if (list.isEmpty()) {
            layoutEmptyEvents.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            layoutEmptyEvents.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
            adapter.setEvents(list);
        }
    }
}