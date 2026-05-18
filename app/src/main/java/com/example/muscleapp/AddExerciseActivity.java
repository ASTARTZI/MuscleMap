package com.example.muscleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddExerciseActivity extends AppCompatActivity {

    private EditText etTitleEn, etDescEn, etTitleEl, etDescEl, etMuscleGroup, etImageName;
    private ExerciseDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_exercise);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_exercise_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new ExerciseDBHandler(this);

        // Language buttons
        View btnEn = findViewById(R.id.btn_en);
        View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        etTitleEn = findViewById(R.id.add_title_en);
        etDescEn = findViewById(R.id.add_desc_en);
        etTitleEl = findViewById(R.id.add_title_el);
        etDescEl = findViewById(R.id.add_desc_el);
        etMuscleGroup = findViewById(R.id.add_muscle_group);
        etImageName = findViewById(R.id.add_image_name);
        Button btnSave = findViewById(R.id.btn_save_exercise);

        btnSave.setOnClickListener(v -> saveExercise());
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void saveExercise() {
        String titleEn = etTitleEn.getText().toString().trim();
        String descEn = etDescEn.getText().toString().trim();
        String titleEl = etTitleEl.getText().toString().trim();
        String descEl = etDescEl.getText().toString().trim();
        String muscleGroup = etMuscleGroup.getText().toString().trim();
        String imageName = etImageName.getText().toString().trim();

        if (titleEn.isEmpty() || descEn.isEmpty() || titleEl.isEmpty() || descEl.isEmpty() || 
            muscleGroup.isEmpty() || imageName.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save English version (passing empty string for tags)
        dbHandler.addExercise(titleEn, descEn, muscleGroup, imageName, "en", "");
        // Save Greek version (passing empty string for tags)
        dbHandler.addExercise(titleEl, descEl, muscleGroup, imageName, "el", "");

        Toast.makeText(this, "Exercise added successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
