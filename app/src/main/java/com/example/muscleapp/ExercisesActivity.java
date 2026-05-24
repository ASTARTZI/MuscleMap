package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.AutoCompleteTextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExercisesActivity extends AppCompatActivity {

    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList;
    private List<Exercise> filteredList;
    private ExerciseDBHandler dbHandler;
    private final Set<String> selectedTags = new HashSet<>();
    private String currentSearchQuery = "";
    private String currentMuscleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.exercises);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.exercises), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_exercises);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        dbHandler = new ExerciseDBHandler(this);

        // Get the muscle group from the intent
        Intent intent = getIntent();
        currentMuscleGroup = intent.getStringExtra("MUSCLE_GROUP");
        if (currentMuscleGroup == null) {
            currentMuscleGroup = "chest";   // fallback
        }

        // Setup language buttons (from Version 2) with list refresh
        View btnEn = findViewById(R.id.btn_en);
        View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> {
            setLocale("en");
            reloadExercises();
        });
        if (btnEl != null) btnEl.setOnClickListener(v -> {
            setLocale("el");
            reloadExercises();
        });

        // Initial load of exercises
        reloadExercises();

        adapter = new ExerciseAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // Setup Search (from Version 1)
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            AutoCompleteTextView searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchEditText != null) {
                searchEditText.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                searchEditText.setHintTextColor(android.graphics.Color.parseColor("#E0E0E0"));
            }
            
            // Remove the default search plate background (underline)
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

        // Setup Filter (from Version 1)
        ImageButton btnFilter = findViewById(R.id.btn_filter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void reloadExercises() {
        // Get current language
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty() && locales.get(0) != null) {
            lang = locales.get(0).getLanguage();
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        if (!"el".equals(lang)) lang = "en";

        // Load from database
        exerciseList = dbHandler.getExercisesByMuscleGroup(currentMuscleGroup, lang);
        filteredList = new ArrayList<>(exerciseList);

        // Reset filters
        selectedTags.clear();
        currentSearchQuery = "";

        // Update adapter if it exists
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void showFilterDialog() {
        Set<String> allTagsSet = new HashSet<>();
        for (Exercise e : exerciseList) {
            String tagsRaw = e.getTags();
            if (tagsRaw != null && !tagsRaw.isEmpty()) {
                String[] tagsArray = tagsRaw.split(",");
                for (String tag : tagsArray) {
                    allTagsSet.add(tag.trim());
                }
            }
        }

        String[] allTags = allTagsSet.toArray(new String[0]);
        boolean[] checkedItems = new boolean[allTags.length];
        for (int i = 0; i < allTags.length; i++) {
            if (selectedTags.contains(allTags[i])) {
                checkedItems[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_description);
        builder.setMultiChoiceItems(allTags, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedTags.add(allTags[which]);
            } else {
                selectedTags.remove(allTags[which]);
            }
        });
        builder.setPositiveButton(R.string.apply, (dialog, which) -> applyFilters());
        builder.setNegativeButton(R.string.clear_all, (dialog, which) -> {
            selectedTags.clear();
            applyFilters();
        });
        builder.show();
    }

    private void applyFilters() {
        List<Exercise> newList = new ArrayList<>();
        for (Exercise e : exerciseList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    e.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    (e.getTags() != null && e.getTags().toLowerCase().contains(currentSearchQuery.toLowerCase()));

            boolean matchesFilter = selectedTags.isEmpty();
            if (!selectedTags.isEmpty() && e.getTags() != null) {
                String[] eTags = e.getTags().split(",");
                for (String t : eTags) {
                    if (selectedTags.contains(t.trim())) {
                        matchesFilter = true;
                        break;
                    }
                }
            }

            if (matchesSearch && matchesFilter) {
                newList.add(e);
            }
        }
        filteredList = newList;
        adapter.updateList(newList);
    }
}