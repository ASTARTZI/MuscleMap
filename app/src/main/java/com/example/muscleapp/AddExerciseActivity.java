package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

    private EditText etTitle, etDesc, etTags;
    private Spinner muscleSpinner;
    private TextView tvImagePath;
    private ExerciseDBHandler dbHandler;
    private String selectedImageUri = "";

    private static final String[] MUSCLE_KEYS = {
            "chest", "arm", "legs", "shoulders", "back", "abs"
    };

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

        // Back button
        View btnBack = findViewById(R.id.btn_back_add);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.add_title);
        etDesc = findViewById(R.id.add_desc);
        etTags = findViewById(R.id.add_tags);
        muscleSpinner = findViewById(R.id.add_muscle_spinner);
        tvImagePath = findViewById(R.id.tv_image_path);
        
        setupMuscleSpinner();

        Button btnPick = findViewById(R.id.btn_pick_image);
        btnPick.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        Button btnSave = findViewById(R.id.btn_save_exercise);
        btnSave.setOnClickListener(v -> saveExercise());
    }

    private void setupMuscleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, buildMuscleLabels());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        muscleSpinner.setAdapter(adapter);
    }

    private String[] buildMuscleLabels() {
        String[] labels = new String[MUSCLE_KEYS.length];
        for (int i = 0; i < MUSCLE_KEYS.length; i++) {
            int resId = getResources().getIdentifier(MUSCLE_KEYS[i], "string", getPackageName());
            labels[i] = resId != 0 ? getString(resId) : MUSCLE_KEYS[i];
        }
        return labels;
    }

    private void saveExercise() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String tags = etTags.getText().toString().trim();
        
        int selectedPos = muscleSpinner.getSelectedItemPosition();
        if (selectedPos < 0) return;
        String muscleGroup = MUSCLE_KEYS[selectedPos];

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, R.string.mandatory_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // If no image selected, use placeholder
        String finalImage = selectedImageUri.isEmpty() ? "ic_placeholder" : selectedImageUri;

        // Auto-translate logic: Saving the same English text for both language versions
        dbHandler.addExercise(title, desc, muscleGroup, finalImage, "en", tags);
        dbHandler.addExercise(title, desc, muscleGroup, finalImage, "el", tags);

        Toast.makeText(this, R.string.exercise_added_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
