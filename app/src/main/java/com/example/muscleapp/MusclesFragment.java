package com.example.muscleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MusclesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_muscles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Language buttons
        View btnEn = view.findViewById(R.id.btn_en);
        View btnEl = view.findViewById(R.id.btn_el);
        if (btnEn != null) btnEn.setOnClickListener(v -> setLocale("en"));
        if (btnEl != null) btnEl.setOnClickListener(v -> setLocale("el"));

        // Muscle group buttons
        setupButton(view, R.id.chest_button, "chest");
        setupButton(view, R.id.arm_button, "arm");
        setupButton(view, R.id.legs_button, "legs");
        setupButton(view, R.id.shoulders_button, "shoulders");
        setupButton(view, R.id.back_button, "back");
        setupButton(view, R.id.abs_button, "abs");

        // Logout button
        Button logoutBtn = view.findViewById(R.id.logout_button);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                ProgramManager.getInstance().clear();
                
                if (getActivity() != null) {
                    getActivity().getSharedPreferences("MuscleAppPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("is_admin", false)
                            .apply();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }
    }

    private void setLocale(String lang) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void setupButton(View parent, int buttonId, final String muscleGroup) {
        Button button = parent.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ExercisesActivity.class);
                intent.putExtra("MUSCLE_GROUP", muscleGroup);
                startActivity(intent);
            });
        }
    }
}
