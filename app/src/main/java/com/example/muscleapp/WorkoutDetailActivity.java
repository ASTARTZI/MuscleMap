package com.example.muscleapp;

import android.app.Dialog;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlanExerciseAdapter adapter;
    private Workout workout;
    private int workoutIndex;
    private TextView emptyText;
    private ExerciseDBHandler dbHandler;

    private static final String[] MUSCLE_KEYS = {
            "chest", "arm", "legs", "shoulders", "back", "abs"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_detail);

        workoutIndex = getIntent().getIntExtra("WORKOUT_INDEX", -1);
        if (workoutIndex == -1) {
            finish();
            return;
        }

        workout = ProgramManager.getInstance().getWorkouts().get(workoutIndex);
        dbHandler = new ExerciseDBHandler(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_back_detail).setOnClickListener(v -> finish());
        TextView titleTV = findViewById(R.id.workout_title_text);
        titleTV.setText(workout.getName());

        findViewById(R.id.btn_edit_workout_title).setOnClickListener(v -> showEditTitleDialog(titleTV));

        recyclerView = findViewById(R.id.workout_exercise_recycler);
        emptyText = findViewById(R.id.workout_empty_text);

        adapter = new PlanExerciseAdapter(this, workout.getExercises(), position -> {
            workout.getExercises().remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, workout.getExercises().size());
            ProgramManager.getInstance().saveProgram(this);
            updateEmptyState();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        updateEmptyState();

        FloatingActionButton fab = findViewById(R.id.fab_add_exercise_to_workout);
        fab.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void showEditTitleDialog(TextView titleTV) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_workout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView dialogTitleTV = dialog.findViewById(R.id.dialog_workout_title);
        EditText nameET = dialog.findViewById(R.id.dialog_workout_name);
        Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn = dialog.findViewById(R.id.dialog_add_btn);

        dialogTitleTV.setText(R.string.update_workout);
        nameET.setText(workout.getName());
        addBtn.setText(R.string.update);
        
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        addBtn.setOnClickListener(v -> {
            String newName = nameET.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, R.string.enter_name_error, Toast.LENGTH_SHORT).show();
                return;
            }
            workout.setName(newName);
            titleTV.setText(newName);
            ProgramManager.getInstance().saveProgram(this);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateEmptyState() {
        if (workout.getExercises().isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private String getCurrentLang() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty() && locales.get(0) != null) {
            lang = locales.get(0).getLanguage();
        } else {
            lang = java.util.Locale.getDefault().getLanguage();
        }
        return "el".equals(lang) ? "el" : "en";
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

        Spinner muscleSpinner = dialog.findViewById(R.id.dialog_muscle_spinner);
        Spinner exerciseSpinner = dialog.findViewById(R.id.dialog_exercise_spinner);
        Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn = dialog.findViewById(R.id.dialog_add_btn);

        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, buildMuscleLabels());
        muscleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        muscleSpinner.setAdapter(muscleAdapter);

        final List<Exercise>[] exercisesRef = new List[]{new ArrayList<>()};

        muscleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                exercisesRef[0] = dbHandler.getExercisesByMuscleGroup(MUSCLE_KEYS[pos], lang);
                List<String> titles = new ArrayList<>();
                for (Exercise e : exercisesRef[0]) titles.add(e.getTitle());
                ArrayAdapter<String> exAdapter = new ArrayAdapter<>(
                        WorkoutDetailActivity.this,
                        R.layout.spinner_item, titles);
                exAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                exerciseSpinner.setAdapter(exAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        muscleSpinner.setSelection(0);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            int exPos = exerciseSpinner.getSelectedItemPosition();
            if (exercisesRef[0] == null || exercisesRef[0].isEmpty() || exPos < 0) {
                Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                return;
            }
            Exercise chosen = exercisesRef[0].get(exPos);
            ExerciseItem item = new ExerciseItem(
                    chosen.getTitle(),
                    chosen.getDescription(),
                    chosen.getMuscleGroup(),
                    chosen.getImageName(),
                    chosen.getTags(),
                    0, 0f
            );
            workout.getExercises().add(item);
            adapter.notifyItemInserted(workout.getExercises().size() - 1);
            ProgramManager.getInstance().saveProgram(this);
            updateEmptyState();
            dialog.dismiss();
        });

        dialog.show();
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
