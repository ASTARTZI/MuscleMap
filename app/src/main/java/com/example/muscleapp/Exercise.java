package com.example.muscleapp;

import java.io.Serializable;

public class Exercise implements Serializable {
    private int id;
    private String title;
    private String description;
    private String muscleGroup;
    private String imageName; // Changed from int imageResId to String imageName
    private String tags;

    // Constructor for insertion (no ID)
    public Exercise(String title, String description, String muscleGroup, String imageName, String tags) {
        this.title = title;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.imageName = imageName;
        this.tags = tags;
    }

    // Full constructor
    public Exercise(int id, String title, String description, String muscleGroup, String imageName, String tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.imageName = imageName;
        this.tags = tags;
    }

    // Getters and setters …
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
