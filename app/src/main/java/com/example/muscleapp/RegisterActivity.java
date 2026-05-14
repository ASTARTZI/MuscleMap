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


public class RegisterActivity extends AppCompatActivity {

    private EditText emailET;
    private EditText passwordET;
    private Button registerbtn;
    private Button loginbtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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

        emailET=findViewById(R.id.emailET);
        passwordET=findViewById(R.id.passwordET);
        registerbtn=findViewById(R.id.registerbtn);
        loginbtn=findViewById(R.id.loginbtn);
        mAuth=FirebaseAuth.getInstance();

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButtonClicked();
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    public void registerButtonClicked(){
        String email=emailET.getText().toString();
        String password=passwordET.getText().toString();
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "The fields are incomplete", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){
            Toast.makeText(this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(this, "Successful registration", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error")
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }
}
