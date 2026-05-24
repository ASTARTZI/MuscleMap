package com.example.muscleapp;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GymProgramActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlanExerciseAdapter adapter;
    private ArrayList<ExerciseItem> programList;
    private TextView emptyText;
    private ExerciseDBHandler dbHandler;

    private static final String[] MUSCLE_KEYS = {
            "chest", "arm", "legs", "shoulders", "back", "abs"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gym_program);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        dbHandler   = new ExerciseDBHandler(this);
        recyclerView = findViewById(R.id.plan_recycler_view);
        emptyText    = findViewById(R.id.plan_empty_text);
        programList  = new ArrayList<>();

        adapter = new PlanExerciseAdapter(this, programList, position -> {
            programList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, programList.size());
            updateEmptyState();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Handle incoming online import
        String importJson = getIntent().getStringExtra("IMPORT_JSON");
        if (importJson != null) {
            importProgramFromCode(importJson, getCurrentLang());
        }

        updateEmptyState();

        FloatingActionButton fab = findViewById(R.id.fab_add_exercise);
        fab.setOnClickListener(v -> showAddExerciseDialog());

        findViewById(R.id.btn_share_program).setOnClickListener(v -> shareProgramAsCode());
        findViewById(R.id.btn_import_program).setOnClickListener(v -> showImportDialog());
        
        Button btnSaveOnline = findViewById(R.id.btn_save_online);
        if (btnSaveOnline != null) {
            btnSaveOnline.setOnClickListener(v -> saveProgramToFirestore());
        }

        bottomNav.setSelectedItemId(R.id.nav_program);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(GymProgramActivity.this, MainActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_online_programs) {
                    startActivity(new Intent(GymProgramActivity.this, OnlineActivity.class));
                    finish();
                    return true;
                }
                return true;
            }
        });
    }

    private String getCurrentLang() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty() && locales.get(0) != null) {
            lang = locales.get(0).getLanguage();
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        return "el".equals(lang) ? "el" : "en";
    }

    private void updateEmptyState() {
        if (programList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddExerciseDialog() {
        String lang = getCurrentLang();

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_exercise);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        Spinner muscleSpinner   = dialog.findViewById(R.id.dialog_muscle_spinner);
        Spinner exerciseSpinner = dialog.findViewById(R.id.dialog_exercise_spinner);
        Button cancelBtn        = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn           = dialog.findViewById(R.id.dialog_add_btn);

        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, buildMuscleLabels());
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        muscleSpinner.setAdapter(muscleAdapter);

        final List<Exercise>[] exercisesRef = new List[]{new ArrayList<>()};

        muscleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                exercisesRef[0] = dbHandler.getExercisesByMuscleGroup(MUSCLE_KEYS[pos], lang);
                List<String> titles = new ArrayList<>();
                for (Exercise e : exercisesRef[0]) titles.add(e.getTitle());
                ArrayAdapter<String> exAdapter = new ArrayAdapter<>(
                        GymProgramActivity.this,
                        android.R.layout.simple_spinner_item, titles);
                exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                exerciseSpinner.setAdapter(exAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        muscleSpinner.setSelection(0);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            int exPos = exerciseSpinner.getSelectedItemPosition();
            if (exercisesRef[0] == null || exercisesRef[0].isEmpty() || exPos < 0) {
                Toast.makeText(this, "Please select an exercise", Toast.LENGTH_SHORT).show();
                return;
            }
            Exercise chosen = exercisesRef[0].get(exPos);
            ExerciseItem item = new ExerciseItem(
                    chosen.getTitle(),
                    chosen.getDescription(),
                    chosen.getMuscleGroup(),
                    chosen.getImageName(),
                    chosen.getTags(),   // ← FIXED: added tags
                    0, 0f
            );
            programList.add(item);
            adapter.notifyItemInserted(programList.size() - 1);
            updateEmptyState();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveProgramToFirestore() {
        if (programList.isEmpty()) {
            Toast.makeText(this, "Program is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        
        try {
            JSONArray arr = new JSONArray();
            for (ExerciseItem item : programList) {
                JSONObject obj = new JSONObject();
                obj.put("image", item.getImageName());
                arr.put(obj);
            }
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("program_json", arr.toString());
            data.put("last_updated", System.currentTimeMillis());
            
            db.collection("users").document(auth.getUid()).collection("data").document("program").set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Program saved online!", Toast.LENGTH_SHORT).show());
                
        } catch (Exception e) {
            Toast.makeText(this, "Error saving program", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProgramAsCode() {
        if (programList.isEmpty()) {
            Toast.makeText(this, "Your program is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONArray arr = new JSONArray();
            for (ExerciseItem item : programList) {
                JSONObject obj = new JSONObject();
                obj.put("image", item.getImageName());
                arr.put(obj);
            }
            String code = arr.toString();

            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("gym_program", code));

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, code);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Gym Program");
            startActivity(Intent.createChooser(shareIntent, "Share Program via…"));

        } catch (Exception e) {
            Toast.makeText(this, "Error creating share code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportDialog() {
        String lang = getCurrentLang();

        EditText input = new EditText(this);
        input.setHint("Paste program code here");
        input.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        input.setHintTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        input.setBackgroundColor(0xFF2C2C2C);
        input.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
                .setTitle("Import Program")
                .setView(input)
                .setPositiveButton("Import", (dlg, which) -> {
                    String code = input.getText().toString().trim();
                    if (!code.isEmpty()) importProgramFromCode(code, lang);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void importProgramFromCode(String code, String lang) {
        try {
            JSONArray arr = new JSONArray(code);
            int added = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String imageName = obj.getString("image");
                Exercise ex = dbHandler.getExerciseByImageName(imageName, lang);
                if (ex != null) {
                    programList.add(new ExerciseItem(
                            ex.getTitle(), ex.getDescription(),
                            ex.getMuscleGroup(), ex.getImageName(),
                            ex.getTags(),   // ← FIXED: added tags
                            0, 0f));
                    added++;
                }
            }
            if (added > 0) {
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(this, added + " exercise(s) imported!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No valid exercises found in code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid code format", Toast.LENGTH_SHORT).show();
        }
    }

    private String[] buildMuscleLabels() {
        String[] labels = new String[MUSCLE_KEYS.length];
        for (int i = 0; i < MUSCLE_KEYS.length; i++) {
            int resId = getResources().getIdentifier(MUSCLE_KEYS[i], "string", getPackageName());
            labels[i] = resId != 0 ? getString(resId) : MUSCLE_KEYS[i];
        }
        return labels;
    }
}