package com.example.muscleapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class ExerciseDetailActivity extends AppCompatActivity {
    private ExerciseDBHandler dbHandler;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercise_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHandler = new ExerciseDBHandler(this);

        ImageView imageView = findViewById(R.id.detail_image);
        TextView titleTextView = findViewById(R.id.detail_title);
        TextView muscleGroupTextView = findViewById(R.id.detail_muscle_group);
        TextView descriptionTextView = findViewById(R.id.detail_description);

        // Get initial data from intent
        imageName = getIntent().getStringExtra("EXERCISE_IMAGE");

        // Refresh UI with current language
        refreshUI(titleTextView, muscleGroupTextView, descriptionTextView, imageView);
    }

    private void refreshUI(TextView titleTV, TextView muscleTV, TextView descTV, ImageView imgV) {
        // Get current language from AppCompatDelegate
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty() && locales.get(0) != null) {
            lang = locales.get(0).getLanguage();
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        if (!"el".equals(lang)) lang = "en";

        // Fetch the translated exercise from the database
        Exercise exercise = dbHandler.getExerciseByImageName(imageName, lang);

        if (exercise != null) {
            titleTV.setText(exercise.getTitle());
            descTV.setText(exercise.getDescription());
            
            // Translate multiple muscle groups
            String muscleGroupsRaw = exercise.getMuscleGroup();
            String[] groups = muscleGroupsRaw.split(",");
            StringBuilder translatedGroups = new StringBuilder();
            
            for (int i = 0; i < groups.length; i++) {
                String muscleKey = groups[i].trim();
                int muscleResId = getResources().getIdentifier(muscleKey, "string", getPackageName());
                String muscleTranslated = (muscleResId != 0) ? getString(muscleResId) : muscleKey;
                translatedGroups.append(muscleTranslated);
                if (i < groups.length - 1) {
                    translatedGroups.append(", ");
                }
            }
            
            muscleTV.setText(getString(R.string.muscle_group_prefix) + translatedGroups.toString());

            // Load Image
            int resId = getResources().getIdentifier(exercise.getImageName(), "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this)
                        .load(resId)
                        .placeholder(R.drawable.ic_placeholder)
                        .fitCenter()
                        .into(imgV);
            }
        }
    }
}
