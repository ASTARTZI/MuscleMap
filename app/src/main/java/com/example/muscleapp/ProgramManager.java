package com.example.muscleapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramManager {
    private static final String BASE_PREF_NAME = "MuscleAppProgram_";
    private static final String KEY_PROGRAM = "program_json";
    private static ProgramManager instance;
    private List<Workout> workouts;

    public interface PublishCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private ProgramManager() {
        workouts = new ArrayList<>();
    }

    public static synchronized ProgramManager getInstance() {
        if (instance == null) {
            instance = new ProgramManager();
        }
        return instance;
    }

    private String getPrefName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return BASE_PREF_NAME + user.getUid();
        }
        return BASE_PREF_NAME + "guest";
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }

    public void clear() {
        workouts.clear();
    }

    public String getProgramJson(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE);
        return prefs.getString(KEY_PROGRAM, "");
    }

    public void importProgramJson(Context context, String json) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PROGRAM, json).apply();
        loadProgram(context);
    }

    public String serializeWorkouts(List<Workout> selectedWorkouts) {
        try {
            JSONArray programArray = new JSONArray();
            for (Workout workout : selectedWorkouts) {
                JSONObject workoutObj = new JSONObject();
                workoutObj.put("name", workout.getName());

                JSONArray exerciseArray = new JSONArray();
                for (ExerciseItem item : workout.getExercises()) {
                    JSONObject exObj = new JSONObject();
                    exObj.put("title", item.getTitle());
                    exObj.put("desc", item.getDescription());
                    exObj.put("muscle", item.getMuscleGroup());
                    exObj.put("image", item.getImageName());
                    exObj.put("tags", item.getTags());
                    exObj.put("sets", item.getSets());
                    exObj.put("reps", item.getReps());
                    exObj.put("weight", (double) item.getWeight());
                    exObj.put("unit", item.getWeightUnit());
                    exerciseArray.put(exObj);
                }
                workoutObj.put("exercises", exerciseArray);
                programArray.put(workoutObj);
            }
            return programArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<Workout> deserializeWorkouts(String json) {
        List<Workout> list = new ArrayList<>();
        try {
            JSONArray programArray = new JSONArray(json);
            for (int i = 0; i < programArray.length(); i++) {
                JSONObject workoutObj = programArray.getJSONObject(i);
                Workout workout = new Workout(workoutObj.getString("name"));

                JSONArray exerciseArray = workoutObj.getJSONArray("exercises");
                for (int j = 0; j < exerciseArray.length(); j++) {
                    JSONObject exObj = exerciseArray.getJSONObject(j);
                    ExerciseItem item = new ExerciseItem(
                            exObj.getString("title"),
                            exObj.getString("desc"),
                            exObj.getString("muscle"),
                            exObj.getString("image"),
                            exObj.getString("tags"),
                            exObj.optInt("sets", 0),
                            exObj.getInt("reps"),
                            (float) exObj.optDouble("weight", 0.0),
                            exObj.optString("unit", "kg")
                    );
                    workout.addExercise(item);
                }
                list.add(workout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveProgram(Context context) {
        String json = serializeWorkouts(workouts);
        SharedPreferences prefs = context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PROGRAM, json).apply();
    }

    public void loadProgram(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PROGRAM, null);
        workouts.clear();
        if (json != null) {
            workouts.addAll(deserializeWorkouts(json));
        }
    }

    public void publishWorkouts(List<Workout> selectedWorkouts, PublishCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure("User not logged in");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = user.getEmail();
        String userUid = user.getUid();

        for (Workout workout : selectedWorkouts) {
            Map<String, Object> data = new HashMap<>();
            data.put("uploader_email", userEmail);
            data.put("uploader_uid", userUid);
            data.put("workout_name", workout.getName());
            
            List<Workout> singleList = new ArrayList<>();
            singleList.add(workout);
            data.put("workout_json", serializeWorkouts(singleList));
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("exercise_count", workout.getExercises().size());

            db.collection("online_workouts").add(data)
                    .addOnSuccessListener(documentReference -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }
}
