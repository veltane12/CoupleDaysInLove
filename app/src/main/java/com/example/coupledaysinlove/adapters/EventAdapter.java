package com.example.coupledaysinlove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.helpers.DateUtils;
import com.example.coupledaysinlove.helpers.ImageLoaderHelper;
import com.example.coupledaysinlove.models.SpecialEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventDeleteListener {
        void onDelete(SpecialEvent event);
    }

    public interface OnEventEditListener {
        void onEdit(SpecialEvent event);
    }

    private final List<SpecialEvent> eventList = new ArrayList<>();
    private final OnEventDeleteListener deleteListener;
    private final OnEventEditListener editListener;

    public EventAdapter(OnEventDeleteListener deleteListener, OnEventEditListener editListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setEvents(List<SpecialEvent> events) {
        this.eventList.clear();
        if (events != null) {
            this.eventList.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_special_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        SpecialEvent event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());

        String category = event.getCategory() != null ? event.getCategory() : "Evento";
        String dateStr = DateUtils.getEventDisplayDate(event.getDateMillis(), category);
        holder.tvSubtitle.setText(category + " • " + dateStr);

        long daysLeft = DateUtils.getDaysUntilNextOccurrence(event.getDateMillis());
        if (daysLeft == 0) {
            holder.tvCountdownChip.setText("¡Es Hoy! 🎉");
        } else {
            holder.tvCountdownChip.setText("Faltan " + daysLeft + " días");
        }

        if ("Cumpleaños".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_cake);
        } else if ("Aniversario".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_anniversary_rings);
        } else if ("Cita Especial".equalsIgnoreCase(category) || "Cita".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_romantic_date);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_star_event);
        }

        // Description / Memory Note
        if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
            holder.tvDescription.setText(event.getDescription().trim());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Memory Photo
        String photoPath = event.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty() && new File(photoPath).exists()) {
            if (holder.cardPhotoContainer != null) {
                holder.cardPhotoContainer.setVisibility(View.VISIBLE);
            }
            ImageLoaderHelper.loadEventPhotoInto(holder.itemView.getContext(), photoPath, holder.ivPhoto);
        } else {
            if (holder.cardPhotoContainer != null) {
                holder.cardPhotoContainer.setVisibility(View.GONE);
            }
        }

        holder.cardItem.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(event);
            }
        });

        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(event);
                }
            });
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View cardItem;
        ImageView ivIcon, ivPhoto;
        View cardPhotoContainer;
        TextView tvTitle, tvSubtitle, tvDescription, tvCountdownChip;
        ImageButton btnEdit, btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardEventItem);
            ivIcon = itemView.findViewById(R.id.ivEventIcon);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvSubtitle = itemView.findViewById(R.id.tvEventSubtitle);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvCountdownChip = itemView.findViewById(R.id.tvCountdownChip);
            cardPhotoContainer = itemView.findViewById(R.id.cardEventPhotoContainer);
            ivPhoto = itemView.findViewById(R.id.ivEventPhoto);
            btnEdit = itemView.findViewById(R.id.btnEditEvent);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}