package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply system bar insets to root layout (from Version 1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Language buttons
        View btnEn = findViewById(R.id.btn_en);
        View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        // Muscle group buttons
        setupButton(R.id.chest_button, "chest");
        setupButton(R.id.arm_button, "arm");
        setupButton(R.id.legs_button, "legs");
        setupButton(R.id.shoulders_button, "shoulders");
        setupButton(R.id.back_button, "back");
        setupButton(R.id.abs_button, "abs");

        // Logout button (from Version 1, with admin flag cleanup)
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

        // Bottom navigation (from Version 2)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.nav_program) {
                        startActivity(new Intent(MainActivity.this, GymProgramActivity.class));
                        return true;
                    }
                    if (id == R.id.nav_online_programs) {
                        startActivity(new Intent(MainActivity.this, OnlineActivity.class));
                        return true;
                    }
                    // nav_home: already here
                    return true;
                }
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