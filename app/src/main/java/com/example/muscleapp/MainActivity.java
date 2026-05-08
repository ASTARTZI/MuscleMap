package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup button listeners
        setupButton(R.id.chest_button, "chest");
        setupButton(R.id.arm_button, "arm");
        setupButton(R.id.legs_button, "legs");
        setupButton(R.id.shoulders_button, "shoulders");
        setupButton(R.id.back_button, "back");
        setupButton(R.id.abs_button, "abs");
    }

    private void setupButton(int buttonId, final String muscleGroup) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ExercisesActivity.class);
                intent.putExtra("MUSCLE_GROUP", muscleGroup);
                startActivity(intent);
            });
        }
    }
}