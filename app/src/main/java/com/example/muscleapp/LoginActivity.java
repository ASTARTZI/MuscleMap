package com.example.muscleapp;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.core.splashscreen.SplashScreen;

public class LoginActivity extends AppCompatActivity {
    private EditText loginemail;
    private EditText loginpassword;
    private Button signinbtn;
    private Button gotoregisterbtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginemail = findViewById(R.id.loginemailET);
        loginpassword = findViewById(R.id.loginpasswordET);
        signinbtn = findViewById(R.id.signinbtn);
        gotoregisterbtn = findViewById(R.id.gotoregisterbtn);
        mAuth = FirebaseAuth.getInstance();

        // Language selection buttons
        View btnEn = findViewById(R.id.btn_en);
        View btnEl = findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        // Check if user is already signed in
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonClicked();
            }
        });

        gotoregisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void loginButtonClicked() {
        String email = loginemail.getText().toString().trim();
        String password = loginpassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isAdmin = email.equalsIgnoreCase("admin@gmail.com");
                
                // Sync user to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("uid", mAuth.getCurrentUser().getUid());
                db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user);

                // Save admin status in SharedPreferences
                getSharedPreferences("MuscleAppPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_admin", isAdmin)
                        .apply();

                Toast.makeText(this, "Login Successful" + (isAdmin ? " (Admin)" : ""), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Login Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginSuccess(String email, boolean isAdmin) {
        // Sync user to Firestore
        if (mAuth.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> user = new HashMap<>();
            user.put("email", email);
            user.put("uid", mAuth.getCurrentUser().getUid());
            db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user);
        }

        // Save admin status in SharedPreferences
        getSharedPreferences("MuscleAppPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_admin", isAdmin)
                .apply();

        Toast.makeText(this, "Login Successful" + (isAdmin ? " (Admin)" : ""), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
