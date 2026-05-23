package com.example.muscleapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private final Context context;

    public ExerciseAdapter(Context context, List<Exercise> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    public void updateList(List<Exercise> newList) {
        this.exercises = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.textTitle.setText(exercise.getTitle());

        String img = exercise.getImageName();
        if (img.startsWith("content://") || img.startsWith("file://")) {
            // Load from device storage
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.ic_placeholder)
                    .fitCenter()
                    .into(holder.imageView);
        } else {
            // Resolve resource ID from name string
            int resId = context.getResources().getIdentifier(img, "drawable", context.getPackageName());
            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        .placeholder(R.drawable.ic_placeholder)
                        .fitCenter()
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }

        // Set click listener to open detail activity
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseDetailActivity.class);
            intent.putExtra("EXERCISE_TITLE", exercise.getTitle());
            intent.putExtra("EXERCISE_DESCRIPTION", exercise.getDescription());
            intent.putExtra("EXERCISE_MUSCLE", exercise.getMuscleGroup());
            intent.putExtra("EXERCISE_IMAGE", exercise.getImageName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        ImageView imageView;
        TextView textTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            imageView = itemView.findViewById(R.id.image_exercise);
            textTitle = itemView.findViewById(R.id.text_title);
        }
    }
}