package com.example.muscleapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
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

    private final ActivityResultLauncher<Intent> addCustomExerciseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String newImageName = result.getData().getStringExtra("NEW_EXERCISE_IMAGE");
                    if (newImageName != null) {
                        Exercise chosen = dbHandler.getExerciseByImageName(newImageName, getCurrentLang());
                        if (chosen != null) {
                            ExerciseItem item = new ExerciseItem(
                                    chosen.getTitle(),
                                    chosen.getDescription(),
                                    chosen.getMuscleGroup(),
                                    chosen.getImageName(),
                                    chosen.getTags(),
                                    0, 0, 0f, "kg"
                            );
                            workout.getExercises().add(item);
                            adapter.notifyDataSetChanged();
                            ProgramManager.getInstance().saveProgram(this);
                            updateEmptyState();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_detail);

        workoutIndex = getIntent().getIntExtra("WORKOUT_INDEX", -1);
        boolean isOnline = getIntent().hasExtra("ONLINE_WORKOUT");
        boolean viewOnly = getIntent().getBooleanExtra("VIEW_ONLY", false);

        if (isOnline) {
            workout = (Workout) getIntent().getSerializableExtra("ONLINE_WORKOUT");
        } else if (workoutIndex != -1) {
            workout = ProgramManager.getInstance().getWorkouts().get(workoutIndex);
        } else {
            finish();
            return;
        }

        dbHandler = new ExerciseDBHandler(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_back_detail).setOnClickListener(v -> finish());
        TextView titleTV = findViewById(R.id.workout_title_text);
        titleTV.setText(workout.getName());

        View btnEdit = findViewById(R.id.btn_edit_workout_title);
        if (viewOnly) {
            btnEdit.setVisibility(View.GONE);
        } else {
            btnEdit.setOnClickListener(v -> showEditTitleDialog(titleTV));
        }

        recyclerView = findViewById(R.id.workout_exercise_recycler);
        emptyText = findViewById(R.id.workout_empty_text);

        adapter = new PlanExerciseAdapter(this, workout.getExercises(), position -> {
            showSetRepsDialog(position);
        }, position -> {
            openExerciseDetail(position);
        });
        
        if (viewOnly) {
            adapter.setViewOnly(true);
        } else {
            // Enable drag and drop reordering for non-view-only mode
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                    int toPosition = target.getAbsoluteAdapterPosition();
                    
                    Collections.swap(workout.getExercises(), fromPosition, toPosition);
                    adapter.notifyItemMoved(fromPosition, toPosition);
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    // Not needed
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    // Save the new order when drag is finished
                    ProgramManager.getInstance().saveProgram(WorkoutDetailActivity.this);
                }
            });
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        updateEmptyState();

        FloatingActionButton fab = findViewById(R.id.fab_add_exercise_to_workout);
        if (viewOnly) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(v -> showAddExerciseDialog());
        }
    }

    private void openExerciseDetail(int position) {
        ExerciseItem item = workout.getExercises().get(position);
        boolean viewOnly = getIntent().getBooleanExtra("VIEW_ONLY", false);
        Intent intent = new Intent(this, ExerciseDetailActivity.class);
        intent.putExtra("EXERCISE_IMAGE", item.getImageName());
        intent.putExtra("VIEW_ONLY", viewOnly);
        startActivity(intent);
    }

    private void showSetRepsDialog(int position) {
        ExerciseItem item = workout.getExercises().get(position);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_set_reps);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView titleTV = dialog.findViewById(R.id.dialog_title);
        EditText etSets = dialog.findViewById(R.id.et_sets);
        EditText etReps = dialog.findViewById(R.id.et_reps);
        EditText etWeight = dialog.findViewById(R.id.et_weight);
        Spinner spinnerUnit = dialog.findViewById(R.id.spinner_unit);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnDelete = dialog.findViewById(R.id.btn_delete_exercise_in_dialog);

        // Dynamic localized title lookup
        Exercise localized = dbHandler.getExerciseByImageName(item.getImageName(), getCurrentLang());
        titleTV.setText(localized != null ? localized.getTitle() : item.getTitle());

        if (item.getSets() > 0) etSets.setText(String.valueOf(item.getSets()));
        if (item.getReps() > 0) etReps.setText(String.valueOf(item.getReps()));
        if (item.getWeight() > 0) etWeight.setText(String.valueOf(item.getWeight()));

        String[] units = {"kg", "lbs"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
        if (item.getWeightUnit().equals("lbs")) spinnerUnit.setSelection(1);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String setsStr = etSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            int sets = setsStr.isEmpty() ? 0 : Integer.parseInt(setsStr);
            int reps = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
            float weight = weightStr.isEmpty() ? 0f : Float.parseFloat(weightStr);
            String unit = units[spinnerUnit.getSelectedItemPosition()];

            item.setSets(sets);
            item.setReps(reps);
            item.setWeight(weight);
            item.setWeightUnit(unit);

            adapter.notifyItemChanged(position);
            ProgramManager.getInstance().saveProgram(this);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_exercise_confirmation)
                    .setPositiveButton(R.string.delete, (d, which) -> {
                        workout.getExercises().remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, workout.getExercises().size());
                        ProgramManager.getInstance().saveProgram(this);
                        updateEmptyState();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        dialog.show();
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
        ListView exerciseListView = dialog.findViewById(R.id.dialog_exercise_list);
        Button btnSelectAll = dialog.findViewById(R.id.btn_select_all_exercises);
        Button btnDeselectAll = dialog.findViewById(R.id.btn_deselect_all_exercises);
        Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn = dialog.findViewById(R.id.dialog_add_btn);

        TextView customBtn = dialog.findViewById(R.id.btn_create_custom_exercise);
        if (customBtn != null) {
            customBtn.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(this, AddExerciseActivity.class);
                addCustomExerciseLauncher.launch(intent);
            });
        }

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
                        R.layout.share_list_item, titles);
                exerciseListView.setAdapter(exAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSelectAll.setOnClickListener(v -> {
            for (int i = 0; i < exercisesRef[0].size(); i++) {
                exerciseListView.setItemChecked(i, true);
            }
        });

        btnDeselectAll.setOnClickListener(v -> {
            for (int i = 0; i < exercisesRef[0].size(); i++) {
                exerciseListView.setItemChecked(i, false);
            }
        });

        muscleSpinner.setSelection(0);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            boolean anyAdded = false;
            for (int i = 0; i < exercisesRef[0].size(); i++) {
                if (exerciseListView.isItemChecked(i)) {
                    Exercise chosen = exercisesRef[0].get(i);
                    ExerciseItem item = new ExerciseItem(
                            chosen.getTitle(),
                            chosen.getDescription(),
                            chosen.getMuscleGroup(),
                            chosen.getImageName(),
                            chosen.getTags(),
                            0, 0, 0f, "kg"
                    );
                    workout.getExercises().add(item);
                    anyAdded = true;
                }
            }

            if (!anyAdded) {
                Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            } else {
                adapter.notifyDataSetChanged();
                ProgramManager.getInstance().saveProgram(this);
                updateEmptyState();
                dialog.dismiss();
            }
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
