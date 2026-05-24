package com.example.muscleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlanExerciseAdapter extends RecyclerView.Adapter<PlanExerciseAdapter.ViewHolder> {

    private final Context context;
    private final List<ExerciseItem> items;
    private final OnDeleteClickListener deleteClickListener;
    private boolean isViewOnly = false;

    public PlanExerciseAdapter(Context context, List<ExerciseItem> items, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.items = items;
        this.deleteClickListener = deleteClickListener;
    }

    public void setViewOnly(boolean viewOnly) {
        this.isViewOnly = viewOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_plan_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseItem item = items.get(position);
        holder.titleTV.setText(item.getTitle());

        int resId = context.getResources().getIdentifier(
                item.getImageName(), "drawable",context.getPackageName());

        if (resId != 0) {
            Glide.with(context)
                    .load(resId)
                    .placeholder(R.drawable.ic_placeholder)
                    .fitCenter()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
        }

        if (isViewOnly) {
            holder.deleteBtn.setVisibility(View.GONE);
        } else {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(holder.getAbsoluteAdapterPosition());
                    }
                }
            });
        }
    }

    public void addItem(ExerciseItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTV;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plan_exercise_image);
            titleTV = itemView.findViewById(R.id.plan_exercise_title);
            deleteBtn = itemView.findViewById(R.id.plan_exercise_delete_btn);
        }
    }
}
