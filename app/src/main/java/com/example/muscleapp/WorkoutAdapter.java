package com.example.muscleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private final Context context;
    private final List<Workout> workouts;
    private final OnWorkoutClickListener listener;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(Workout workout, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public WorkoutAdapter(Context context, List<Workout> workouts, OnWorkoutClickListener listener, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.workouts = workouts;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.nameTV.setText(workout.getName());
        int count = workout.getExercises().size();
        holder.countTV.setText(count + (count == 1 ? " Exercise" : " Exercises"));

        holder.itemView.setOnClickListener(v -> listener.onWorkoutClick(workout, position));
        holder.editBtn.setOnClickListener(v -> editListener.onEditClick(workout, position));
        holder.deleteBtn.setOnClickListener(v -> deleteListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, countTV;
        ImageButton editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.workout_name);
            countTV = itemView.findViewById(R.id.workout_exercise_count);
            editBtn = itemView.findViewById(R.id.workout_edit_btn);
            deleteBtn = itemView.findViewById(R.id.workout_delete_btn);
        }
    }
}
