package com.example.muscleapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7; // Bumped version to refresh DB with new exercises
    private static final String DATABASE_NAME = "exerciseDB.db";
    public static final String TABLE_EXERCISES = "exercises";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_MUSCLE_GROUP = "muscle_group";
    public static final String COLUMN_IMAGE_NAME = "image_name";

    public ExerciseDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_MUSCLE_GROUP + " TEXT,"
                + COLUMN_IMAGE_NAME + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
        insertDefaultExercises(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        onCreate(db);
    }

    public void addExercise(Exercise exercise) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, exercise.getTitle());
        values.put(COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(COLUMN_MUSCLE_GROUP, exercise.getMuscleGroup());
        values.put(COLUMN_IMAGE_NAME, exercise.getImageName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_EXERCISES, null, values);
        db.close();
    }

    public List<Exercise> getExercisesByMuscleGroup(String muscleGroup) {
        List<Exercise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES, null,
                COLUMN_MUSCLE_GROUP + " = ?", new String[]{muscleGroup},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                list.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    private void insertDefaultExercises(SQLiteDatabase db) {
        // Chest
        addExerciseInternal(db, "Incline Bench Press", "Upper chest", "chest", "incline_bench_press");
        addExerciseInternal(db, "Flat Bench Press", "Middle chest", "chest", "flat_bench_press");
        addExerciseInternal(db, "Chest Fly", "Stretches pecs", "chest", "chest_fly");

        // Arms
        addExerciseInternal(db, "Bicep Curl", "Classic arm", "arm", "bicep_curl");
        addExerciseInternal(db, "Tricep Dips", "Triceps", "arm", "tricep_dips");

        // Legs
        addExerciseInternal(db, "Squats", "Quadriceps and glutes", "legs", "squats");
        addExerciseInternal(db, "Leg Press", "Heavy leg focus", "legs", "leg_press");

        // Shoulders
        addExerciseInternal(db, "Overhead Press", "Deltoids", "shoulders", "overhead_press");
        addExerciseInternal(db, "Lateral Raise", "Side deltoids", "shoulders", "lateral_raise");

        // Back
        addExerciseInternal(db, "Pull Ups", "Lats and upper back", "back", "pull_ups");
        addExerciseInternal(db, "Deadlift", "Lower back and posterior chain", "back", "deadlift");

        // Abs
        addExerciseInternal(db, "Crunches", "Upper abdominals", "abs", "crunches");
        addExerciseInternal(db, "Plank", "Core stability", "abs", "plank");
    }

    private void addExerciseInternal(SQLiteDatabase db, String title, String description,
                                     String muscleGroup, String imageName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_MUSCLE_GROUP, muscleGroup);
        values.put(COLUMN_IMAGE_NAME, imageName);
        db.insert(TABLE_EXERCISES, null, values);
    }
}