package com.example.muscleapp;

public class ExerciseItem extends Exercise{
    private int reps;
    private float weightKG;

    public ExerciseItem(String title, String description, String muscleGroup, String imageName, int reps, float weightKG) {
        super(title, description, muscleGroup, imageName);
        this.reps = reps;
        this.weightKG = weightKG;
    }

    public int getReps() {
        return reps;
    }
    public void setReps(int reps) {
        this.reps = reps;
    }
    public float getWeightKG() {
        return weightKG;
    }
    public void setWeightKG(float weightKG) {
        this.weightKG = weightKG;
    }
}
