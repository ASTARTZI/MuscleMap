package com.example.muscleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
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
            }else{
                Toast.makeText(this, "Error: " + task.getException().getMessage()
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }
}