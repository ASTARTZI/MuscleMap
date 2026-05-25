package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnlineFragment extends Fragment {

    private RecyclerView recyclerView;
    private OnlineWorkoutAdapter adapter;
    private List<DocumentSnapshot> allWorkoutDocs;
    private List<DocumentSnapshot> filteredWorkoutDocs;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ExerciseDBHandler dbHandler;

    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.online_recycler_view);
        swipeRefresh = view.findViewById(R.id.online_swipe_refresh);
        progressBar = view.findViewById(R.id.online_progress_bar);
        emptyText = view.findViewById(R.id.online_empty_text);

        dbHandler = new ExerciseDBHandler(requireContext());
        allWorkoutDocs = new ArrayList<>();
        filteredWorkoutDocs = new ArrayList<>();
        
        adapter = new OnlineWorkoutAdapter(requireContext(), filteredWorkoutDocs, 
                doc -> {
                    // Open WorkoutDetailActivity in view-only mode
                    String json = doc.getString("workout_json");
                    List<Workout> list = ProgramManager.getInstance().deserializeWorkouts(json);
                    if (!list.isEmpty()) {
                        Workout w = list.get(0);
                        Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
                        intent.putExtra("ONLINE_WORKOUT", w);
                        intent.putExtra("VIEW_ONLY", true);
                        startActivity(intent);
                    }
                }, 
                doc -> {
                    // Add to my Program
                    addOnlineWorkoutToLocal(doc);
                },
                doc -> {
                    // Delete from Online Gallery
                    showDeleteConfirmation(doc);
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadOnlineWorkouts);

        setupSearch(view);

        loadOnlineWorkouts();
    }

    private void setupSearch(View view) {
        SearchView searchView = view.findViewById(R.id.online_search_view);
        if (searchView != null) {
            AutoCompleteTextView searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchEditText != null) {
                searchEditText.setTextColor(android.graphics.Color.WHITE);
                searchEditText.setHintTextColor(android.graphics.Color.parseColor("#E0E0E0"));
            }
            
            View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
            if (searchPlate != null) {
                searchPlate.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentSearchQuery = query;
                    applyFilters();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentSearchQuery = newText;
                    applyFilters();
                    return true;
                }
            });
        }
    }

    private void loadOnlineWorkouts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("online_workouts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    allWorkoutDocs.clear();
                    allWorkoutDocs.addAll(queryDocumentSnapshots.getDocuments());
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        filteredWorkoutDocs.clear();
        String query = currentSearchQuery.toLowerCase().trim();

        for (DocumentSnapshot doc : allWorkoutDocs) {
            String workoutName = doc.getString("workout_name");
            String uploaderEmail = doc.getString("uploader_email");
            String workoutJson = doc.getString("workout_json");
            
            if (workoutName == null) workoutName = "";
            if (uploaderEmail == null) uploaderEmail = "";
            String uploaderName = uploaderEmail.split("@")[0];

            boolean matchesSearch = query.isEmpty() || 
                    workoutName.toLowerCase().contains(query) || 
                    uploaderName.toLowerCase().contains(query);

            // Check exercises for search (Bilingual Support)
            List<Workout> list = ProgramManager.getInstance().deserializeWorkouts(workoutJson);
            if (!list.isEmpty()) {
                for (ExerciseItem item : list.get(0).getExercises()) {
                    // 1. Check title as stored (language it was published in)
                    if (item.getTitle().toLowerCase().contains(query)) {
                        matchesSearch = true;
                        break;
                    }

                    // 2. Cross-language search: check current translation in DB
                    // (e.g. if workout has Greek title but user searches "incline")
                    Exercise enEx = dbHandler.getExerciseByImageName(item.getImageName(), "en");
                    if (enEx != null && enEx.getTitle().toLowerCase().contains(query)) {
                        matchesSearch = true;
                        break;
                    }

                    Exercise elEx = dbHandler.getExerciseByImageName(item.getImageName(), "el");
                    if (elEx != null && elEx.getTitle().toLowerCase().contains(query)) {
                        matchesSearch = true;
                        break;
                    }
                }
            }

            if (matchesSearch) {
                filteredWorkoutDocs.add(doc);
            }
        }

        adapter.notifyDataSetChanged();
        
        if (filteredWorkoutDocs.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmation(DocumentSnapshot doc) {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_online_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteOnlineWorkout(doc))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteOnlineWorkout(DocumentSnapshot doc) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("online_workouts").document(doc.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                    loadOnlineWorkouts();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addOnlineWorkoutToLocal(DocumentSnapshot doc) {
        String json = doc.getString("workout_json");
        List<Workout> imported = ProgramManager.getInstance().deserializeWorkouts(json);
        if (imported.isEmpty()) return;

        Workout workout = imported.get(0);
        String dateId = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault()).format(new Date());
        workout.setName(workout.getName() + " [" + getString(R.string.imported_from_online) + " " + dateId + "]");

        ProgramManager.getInstance().loadProgram(requireContext());
        ProgramManager.getInstance().getWorkouts().add(workout);
        ProgramManager.getInstance().saveProgram(requireContext());

        Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show();
    }
}
