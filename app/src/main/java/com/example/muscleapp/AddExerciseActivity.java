package com.example.muscleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddExerciseActivity extends AppCompatActivity {

    private EditText etTitle, etDesc, etMuscleGroup;
    private TextView tvImagePath;
    private ExerciseDBHandler dbHandler;
    private String selectedImageUri = "";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    tvImagePath.setText("Image selected");
                    // Grant persistent permission if needed (content URIs)
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
    );

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

        // Back button
        View btnBack = findViewById(R.id.btn_back_add);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.add_title);
        etDesc = findViewById(R.id.add_desc);
        etMuscleGroup = findViewById(R.id.add_muscle_group);
        tvImagePath = findViewById(R.id.tv_image_path);
        
        Button btnPick = findViewById(R.id.btn_pick_image);
        btnPick.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        Button btnSave = findViewById(R.id.btn_save_exercise);
        btnSave.setOnClickListener(v -> saveExercise());
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void saveExercise() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String muscleGroup = etMuscleGroup.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || muscleGroup.isEmpty()) {
            Toast.makeText(this, "Title, Description and Muscle Group are mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        // If no image selected, use placeholder
        String finalImage = selectedImageUri.isEmpty() ? "ic_placeholder" : selectedImageUri;

        // Auto-translate logic: Saving the same English text for both language versions
        // In a real app, you would call a translation API here.
        dbHandler.addExercise(title, desc, muscleGroup, finalImage, "en", "");
        dbHandler.addExercise(title, desc, muscleGroup, finalImage, "el", "");

        Toast.makeText(this, "Exercise added successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
