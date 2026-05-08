package com.example.muscleapp;

public class Exercise {
    private int id;
    private String title;
    private String description;
    private String muscleGroup;
    private String imageName; // Changed from int imageResId to String imageName

    // Constructor for insertion (no ID)
    public Exercise(String title, String description, String muscleGroup, String imageName) {
        this.title = title;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.imageName = imageName;
    }

    // Full constructor
    public Exercise(int id, String title, String description, String muscleGroup, String imageName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.imageName = imageName;
    }

    // Getters and setters …
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMuscleGroup() { return muscleGroup; }
}
