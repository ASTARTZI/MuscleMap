package com.example.muscleapp;

import java.io.Serializable;

public class ExerciseItem extends Exercise implements Serializable {
    private int sets;
    private int reps;
    private float weight;
    private String weightUnit; // "kg" or "lbs"

    public ExerciseItem(String title, String description, String muscleGroup, String imageName, String tags, int sets, int reps, float weight, String weightUnit) {
        super(title, description, muscleGroup, imageName, tags);
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.weightUnit = weightUnit;
    }

    public int getSets() {
        return sets;
    }
    public void setSets(int sets) {
        this.sets = sets;
    }
    public int getReps() {
        return reps;
    }
    public void setReps(int reps) {
        this.reps = reps;
    }
    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public String getWeightUnit() {
        return weightUnit != null ? weightUnit : "kg";
    }
    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }
}
