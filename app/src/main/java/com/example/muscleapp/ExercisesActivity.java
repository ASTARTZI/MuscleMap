package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

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

        // Load exercises from DB
        exerciseList = dbHandler.getExercisesByMuscleGroup(muscleGroup);

        adapter = new ExerciseAdapter(this, exerciseList);
        recyclerView.setAdapter(adapter);
    }
}