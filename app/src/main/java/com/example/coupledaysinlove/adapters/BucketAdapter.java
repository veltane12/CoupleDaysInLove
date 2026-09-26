package com.example.coupledaysinlove.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.helpers.DateUtils;
import com.example.coupledaysinlove.models.BucketItem;

import java.util.ArrayList;
import java.util.List;

public class BucketAdapter extends RecyclerView.Adapter<BucketAdapter.BucketViewHolder> {

    public interface OnBucketCheckChangeListener {
        void onCheckChanged(BucketItem item, boolean isChecked);
    }

    public interface OnBucketEditListener {
        void onEdit(BucketItem item);
    }

    public interface OnBucketDeleteListener {
        void onDelete(BucketItem item);
    }

    private final List<BucketItem> bucketList = new ArrayList<>();
    private final OnBucketCheckChangeListener checkChangeListener;
    private final OnBucketEditListener editListener;
    private final OnBucketDeleteListener deleteListener;

    public BucketAdapter(OnBucketCheckChangeListener checkChangeListener,
                         OnBucketEditListener editListener,
                         OnBucketDeleteListener deleteListener) {
        this.checkChangeListener = checkChangeListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setBucketItems(List<BucketItem> items) {
        this.bucketList.clear();
        if (items != null) {
            this.bucketList.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BucketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bucket_list, parent, false);
        return new BucketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BucketViewHolder holder, int position) {
        BucketItem item = bucketList.get(position);

        holder.tvTitle.setText(item.getTitle());

        String category = item.getCategory() != null ? item.getCategory() : "Otro";
        String statusText;
        if (item.isCompleted()) {
            String dateStr = item.getCompletedDateMillis() > 0 ?
                    DateUtils.getShortDate(item.getCompletedDateMillis()) :
                    DateUtils.getShortDate(System.currentTimeMillis());
            statusText = holder.itemView.getContext().getString(R.string.completed_on, dateStr);

            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.65f);
        } else {
            statusText = holder.itemView.getContext().getString(R.string.pending_status);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1.0f);
        }
        holder.tvSubtitle.setText(category + " • " + statusText);

        // Prevent unwanted listener triggering during recycling
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(item.isCompleted());
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkChangeListener != null) {
                checkChangeListener.onCheckChanged(item, isChecked);
            }
        });

        // Set Icon based on category
        if ("Viajes".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_category_travel);
        } else if ("Citas".equalsIgnoreCase(category) || "Cita".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_romantic_date);
        } else if ("Gastronomía".equalsIgnoreCase(category) || "Comida".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_category_food);
        } else if ("Hogar".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_category_home);
        } else if ("Aventura".equalsIgnoreCase(category)) {
            holder.ivIcon.setImageResource(R.drawable.ic_star_event);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_bucket_list);
        }

        holder.cardItem.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(item);
            }
        });

        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(item);
                }
            });
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bucketList.size();
    }

    static class BucketViewHolder extends RecyclerView.ViewHolder {
        View cardItem;
        CheckBox cbCompleted;
        ImageView ivIcon;
        TextView tvTitle, tvSubtitle;
        ImageButton btnEdit, btnDelete;

        public BucketViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardBucketItem);
            cbCompleted = itemView.findViewById(R.id.cbBucketCompleted);
            ivIcon = itemView.findViewById(R.id.ivBucketCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvBucketTitle);
            tvSubtitle = itemView.findViewById(R.id.tvBucketSubtitle);
            btnEdit = itemView.findViewById(R.id.btnEditBucket);
            btnDelete = itemView.findViewById(R.id.btnDeleteBucket);
        }
    }
}