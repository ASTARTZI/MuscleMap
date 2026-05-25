package com.example.muscleapp;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgramFragment extends Fragment {

    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_program, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.plan_recycler_view);
        emptyText    = view.findViewById(R.id.plan_empty_text);

        ProgramManager.getInstance().loadProgram(requireContext());
        workoutList = ProgramManager.getInstance().getWorkouts();

        adapter = new WorkoutAdapter(requireContext(), workoutList, (workout, position) -> {
            Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
            intent.putExtra("WORKOUT_INDEX", position);
            startActivity(intent);
        }, (workout, position) -> {
            showEditWorkoutDialog(workout, position);
        }, position -> {
            showDeleteWorkoutConfirmation(position);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        updateEmptyState();

        FloatingActionButton fab = view.findViewById(R.id.fab_add_exercise);
        fab.setOnClickListener(v -> showAddWorkoutDialog());

        view.findViewById(R.id.btn_share_program).setOnClickListener(v -> showShareSelectionDialog());
        view.findViewById(R.id.btn_import_program).setOnClickListener(v -> showImportDialog());
        view.findViewById(R.id.btn_save_online).setOnClickListener(v -> showPublishSelectionDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void showDeleteWorkoutConfirmation(int position) {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_workout_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    workoutList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, workoutList.size());
                    ProgramManager.getInstance().saveProgram(requireContext());
                    updateEmptyState();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateEmptyState() {
        if (workoutList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddWorkoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_workout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText nameET = dialog.findViewById(R.id.dialog_workout_name);
        Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn = dialog.findViewById(R.id.dialog_add_btn);

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        addBtn.setOnClickListener(v -> {
            String name = nameET.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.enter_name_error, Toast.LENGTH_SHORT).show();
                return;
            }
            workoutList.add(new Workout(name));
            adapter.notifyItemInserted(workoutList.size() - 1);
            ProgramManager.getInstance().saveProgram(requireContext());
            updateEmptyState();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditWorkoutDialog(Workout workout, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_workout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView titleTV = dialog.findViewById(R.id.dialog_workout_title);
        EditText nameET = dialog.findViewById(R.id.dialog_workout_name);
        Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_btn);
        Button addBtn = dialog.findViewById(R.id.dialog_add_btn);

        titleTV.setText(R.string.update_workout);
        nameET.setText(workout.getName());
        addBtn.setText(R.string.update);

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        addBtn.setOnClickListener(v -> {
            String name = nameET.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.enter_name_error, Toast.LENGTH_SHORT).show();
                return;
            }
            workout.setName(name);
            adapter.notifyItemChanged(position);
            ProgramManager.getInstance().saveProgram(requireContext());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showShareSelectionDialog() {
        if (workoutList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_workouts_to_share, Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_share_selection);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView shareTitleTV = dialog.findViewById(R.id.dialog_share_title);
        shareTitleTV.setText(R.string.select_workouts_to_share);

        ListView listView = dialog.findViewById(R.id.share_list_view);
        Button btnSelectAll = dialog.findViewById(R.id.btn_select_all);
        Button btnDeselectAll = dialog.findViewById(R.id.btn_deselect_all);
        Button btnCancel = dialog.findViewById(R.id.btn_share_cancel);
        Button btnShare = dialog.findViewById(R.id.btn_share_confirm);

        String[] workoutNames = new String[workoutList.size()];
        for (int i = 0; i < workoutList.size(); i++) {
            workoutNames[i] = workoutList.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.share_list_item, workoutNames);
        listView.setAdapter(adapter);

        btnSelectAll.setOnClickListener(v -> {
            for (int i = 0; i < workoutNames.length; i++) {
                listView.setItemChecked(i, true);
            }
        });

        btnDeselectAll.setOnClickListener(v -> {
            for (int i = 0; i < workoutNames.length; i++) {
                listView.setItemChecked(i, false);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnShare.setOnClickListener(v -> {
            List<Workout> selected = new ArrayList<>();
            for (int i = 0; i < workoutList.size(); i++) {
                if (listView.isItemChecked(i)) {
                    selected.add(workoutList.get(i));
                }
            }
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            } else {
                shareSelectedWorkouts(selected);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showPublishSelectionDialog() {
        if (workoutList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_workouts_to_share, Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_share_selection);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView titleTV = dialog.findViewById(R.id.dialog_share_title);
        titleTV.setText(R.string.select_workouts_to_publish);

        ListView listView = dialog.findViewById(R.id.share_list_view);
        Button btnSelectAll = dialog.findViewById(R.id.btn_select_all);
        Button btnDeselectAll = dialog.findViewById(R.id.btn_deselect_all);
        Button btnCancel = dialog.findViewById(R.id.btn_share_cancel);
        Button btnPublish = dialog.findViewById(R.id.btn_share_confirm);
        btnPublish.setText(R.string.publish);

        String[] workoutNames = new String[workoutList.size()];
        for (int i = 0; i < workoutList.size(); i++) {
            workoutNames[i] = workoutList.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.share_list_item, workoutNames);
        listView.setAdapter(adapter);

        btnSelectAll.setOnClickListener(v -> {
            for (int i = 0; i < workoutNames.length; i++) {
                listView.setItemChecked(i, true);
            }
        });

        btnDeselectAll.setOnClickListener(v -> {
            for (int i = 0; i < workoutNames.length; i++) {
                listView.setItemChecked(i, false);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnPublish.setOnClickListener(v -> {
            List<Workout> selected = new ArrayList<>();
            for (int i = 0; i < workoutList.size(); i++) {
                if (listView.isItemChecked(i)) {
                    selected.add(workoutList.get(i));
                }
            }
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            } else {
                publishSelectedWorkouts(selected);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void publishSelectedWorkouts(List<Workout> selected) {
        Toast.makeText(requireContext(), R.string.publishing, Toast.LENGTH_SHORT).show();
        ProgramManager.getInstance().publishWorkouts(selected, new ProgramManager.PublishCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.publish_success, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void shareSelectedWorkouts(List<Workout> selected) {
        String code = ProgramManager.getInstance().serializeWorkouts(selected);
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("gym_program", code));
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, code);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Gym Workouts");
        startActivity(Intent.createChooser(shareIntent, "Share via…"));
    }

    private void showImportDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Paste program code here");
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFFAAAAAA);
        input.setBackgroundColor(0xFF2C2C2C);
        input.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.import_workouts)
                .setView(input)
                .setPositiveButton(R.string.import_text, (dlg, which) -> {
                    String code = input.getText().toString().trim();
                    if (!code.isEmpty()) {
                        importAdditively(code);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void importAdditively(String json) {
        List<Workout> imported = ProgramManager.getInstance().deserializeWorkouts(json);
        if (imported.isEmpty()) {
            Toast.makeText(requireContext(), R.string.invalid_code_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique identifier for this import session based on current date/time
        String dateId = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault()).format(new Date());
        
        for (Workout w : imported) {
            // Strip any existing identifiers (old random ones or previous timestamps)
            String cleanName = w.getName().replaceAll("\\s\\[Imp-.*?\\]", "")
                                        .replaceAll("\\s\\[\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{2}:\\d{2}\\]", "");
            w.setName(cleanName + " [" + dateId + "]");
            workoutList.add(w);
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
        ProgramManager.getInstance().saveProgram(requireContext());
        Toast.makeText(requireContext(), getString(R.string.workouts_imported, imported.size()), Toast.LENGTH_SHORT).show();
    }
}
