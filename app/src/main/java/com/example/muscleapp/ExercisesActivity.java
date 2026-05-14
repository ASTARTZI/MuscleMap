package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExercisesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList;
    private ExerciseDBHandler dbHandler;

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

        // Setup language buttons
        android.view.View btnEn = findViewById(R.id.btn_en);
        android.view.View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        recyclerView = findViewById(R.id.recycler_exercises);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        dbHandler = new ExerciseDBHandler(this);

        // Get the muscle group from the intent
        Intent intent = getIntent();
        String muscleGroup = intent.getStringExtra("MUSCLE_GROUP");
        if (muscleGroup == null) {
            muscleGroup = "chest";   // fallback
        }

        // Get current language from AppCompatDelegate
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty() && locales.get(0) != null) {
            lang = locales.get(0).getLanguage();
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        if (!"el".equals(lang)) lang = "en";

        // Load exercises from DB
        exerciseList = dbHandler.getExercisesByMuscleGroup(muscleGroup, lang);

        adapter = new ExerciseAdapter(this, exerciseList);
        recyclerView.setAdapter(adapter);
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }
}
