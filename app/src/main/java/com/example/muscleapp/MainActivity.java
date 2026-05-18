package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

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

        // Setup language buttons
        View btnEn = findViewById(R.id.btn_en);
        View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        // Setup button listeners
        setupButton(R.id.chest_button, "chest");
        setupButton(R.id.arm_button, "arm");
        setupButton(R.id.legs_button, "legs");
        setupButton(R.id.shoulders_button, "shoulders");
        setupButton(R.id.back_button, "back");
        setupButton(R.id.abs_button, "abs");

        // Logout button
        Button logoutBtn = findViewById(R.id.logout_button);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                // Clear admin status on logout
                getSharedPreferences("MuscleAppPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_admin", false)
                        .apply();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
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
