package com.example.muscleapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Workout implements Serializable {
    private String name;
    private List<ExerciseItem> exercises;

    public Workout(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExerciseItem> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseItem> exercises) {
        this.exercises = exercises;
    }
    
    public void addExercise(ExerciseItem item) {
        this.exercises.add(item);
    }
}
