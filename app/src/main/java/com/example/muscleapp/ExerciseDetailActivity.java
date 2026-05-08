package com.example.muscleapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ExerciseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        ImageView imageView = findViewById(R.id.detail_image);
        TextView titleTextView = findViewById(R.id.detail_title);
        TextView muscleGroupTextView = findViewById(R.id.detail_muscle_group);
        TextView descriptionTextView = findViewById(R.id.detail_description);

        // Get data from intent
        String title = getIntent().getStringExtra("EXERCISE_TITLE");
        String description = getIntent().getStringExtra("EXERCISE_DESCRIPTION");
        String muscleGroup = getIntent().getStringExtra("EXERCISE_MUSCLE");
        String imageName = getIntent().getStringExtra("EXERCISE_IMAGE");

        titleTextView.setText(title);
        muscleGroupTextView.setText("Muscle Group: " + muscleGroup);
        descriptionTextView.setText(description);

        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            Glide.with(this)
                .load(resId)
                .placeholder(R.drawable.ic_placeholder)
                .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }
}