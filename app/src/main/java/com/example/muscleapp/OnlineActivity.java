package com.example.muscleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineActivity extends AppCompatActivity implements FriendAdapter.OnFriendActionListener, ReceivedProgramAdapter.OnImportListener {

    private EditText etEmail;
    private RecyclerView rvFriends, rvReceived;
    private FriendAdapter friendAdapter;
    private ReceivedProgramAdapter receivedAdapter;
    private List<Map<String, Object>> friendsList = new ArrayList<>();
    private List<Map<String, Object>> receivedList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_nav), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_friend_email);
        rvFriends = findViewById(R.id.rv_friends);
        rvReceived = findViewById(R.id.rv_received);

        friendAdapter = new FriendAdapter(friendsList, this);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(friendAdapter);

        receivedAdapter = new ReceivedProgramAdapter(receivedList, this);
        rvReceived.setLayoutManager(new LinearLayoutManager(this));
        rvReceived.setAdapter(receivedAdapter);

        findViewById(R.id.btn_find_friend).setOnClickListener(v -> findFriend());

        loadFriends();
        loadReceivedPrograms();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_online_programs);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_program) {
                startActivity(new Intent(this, GymProgramActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }

    private void findFriend() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) return;

        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    addFriend(doc);
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void addFriend(DocumentSnapshot userDoc) {
        String myUid = auth.getUid();
        String friendUid = userDoc.getId();
        String friendEmail = userDoc.getString("email");

        if (myUid == null) return;
        if (myUid.equals(friendUid)) {
            Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> friend = new HashMap<>();
        friend.put("uid", friendUid);
        friend.put("email", friendEmail);

        db.collection("users").document(myUid).collection("friends").document(friendUid).set(friend)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show();
                loadFriends();
            });
    }

    private void loadFriends() {
        if (auth.getUid() == null) return;
        db.collection("users").document(auth.getUid()).collection("friends").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                friendsList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    friendsList.add(doc.getData());
                }
                friendAdapter.notifyDataSetChanged();
            });
    }

    private void loadReceivedPrograms() {
        if (auth.getUid() == null) return;
        db.collection("users").document(auth.getUid()).collection("received_programs").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                receivedList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    receivedList.add(doc.getData());
                }
                receivedAdapter.notifyDataSetChanged();
            });
    }

    @Override
    public void onShareProgram(String friendUid) {
        if (auth.getUid() == null) return;
        db.collection("users").document(auth.getUid()).collection("data").document("program").get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null && auth.getCurrentUser() != null) {
                        data.put("from_email", auth.getCurrentUser().getEmail());
                        db.collection("users").document(friendUid).collection("received_programs").document(auth.getUid()).set(data)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Program sent!", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(this, "Save your program online first!", Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onViewProgram(String friendUid) {
        Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImport(String json) {
        Intent intent = new Intent(this, GymProgramActivity.class);
        intent.putExtra("IMPORT_JSON", json);
        startActivity(intent);
        finish();
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }
}
