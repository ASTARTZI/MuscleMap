package com.example.muscleapp;

import java.util.ArrayList;

public class DayPlan {
    private String DayName;
    private ArrayList<ExerciseItem> exercises;

    public DayPlan(String dayName) {
        DayName = dayName;
        this.exercises = new ArrayList<>();
    }

    public String getDayName() {
        return DayName;
    }

    public void setDayName(String dayName) {
        DayName = dayName;
    }

    public ArrayList<ExerciseItem> getExercises() {
        return exercises;
    }
}
