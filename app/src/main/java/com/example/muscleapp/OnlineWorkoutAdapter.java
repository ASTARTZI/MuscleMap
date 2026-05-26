package com.example.muscleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class OnlineWorkoutAdapter extends RecyclerView.Adapter<OnlineWorkoutAdapter.ViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> workouts;
    private final OnWorkoutClickListener listener;
    private final OnAddClickListener addListener;
    private final OnDeleteClickListener deleteListener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(DocumentSnapshot workout);
    }

    public interface OnAddClickListener {
        void onAddClick(DocumentSnapshot workout);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(DocumentSnapshot workout);
    }

    public OnlineWorkoutAdapter(Context context, List<DocumentSnapshot> workouts, 
                                OnWorkoutClickListener listener, OnAddClickListener addListener, 
                                OnDeleteClickListener deleteListener) {
        this.context = context;
        this.workouts = workouts;
        this.listener = listener;
        this.addListener = addListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_online_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = workouts.get(position);
        String name = doc.getString("workout_name");
        String email = doc.getString("uploader_email");
        String uploaderUid = doc.getString("uploader_uid");
        Long count = doc.getLong("exercise_count");

        holder.nameTV.setText(name);
        
        // Extract username from email
        String username = email != null ? email.split("@")[0] : "anonymous";
        holder.uploaderTV.setText(context.getString(R.string.by_user, username));
        
        holder.countTV.setText(context.getString(R.string.exercises_count, count != null ? count.intValue() : 0));

        // Visibility of delete button
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAdmin = context.getSharedPreferences("MuscleMapPrefs", Context.MODE_PRIVATE)
                .getBoolean("is_admin", false);

        if (currentUser != null && ((uploaderUid != null && uploaderUid.equals(currentUser.getUid())) || isAdmin)) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onWorkoutClick(doc));
        holder.addBtn.setOnClickListener(v -> addListener.onAddClick(doc));
        holder.deleteBtn.setOnClickListener(v -> deleteListener.onDeleteClick(doc));
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, uploaderTV, countTV;
        Button addBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.online_workout_name);
            uploaderTV = itemView.findViewById(R.id.online_uploader_name);
            countTV = itemView.findViewById(R.id.online_exercise_count);
            addBtn = itemView.findViewById(R.id.btn_add_online);
            deleteBtn = itemView.findViewById(R.id.btn_delete_online);
        }
    }
}
