package com.example.muscleapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 12; // Incremented to prevent downgrade crashes after rollback
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
        addExerciseInternal(db, "Incline Bench Press",
                "The Incline Bench Press mainly targets the upper chest while also engaging the shoulders and triceps.\n" +
                        "Adjust the bench to an incline of about 30 to 45 degrees for better upper chest activation.\n" +
                        "Keep your feet flat on the floor and maintain a stable body position throughout the exercise.\n" +
                        "Lower the bar slowly toward the upper chest area with control and proper alignment.\n" +
                        "Avoid bouncing the bar or locking the elbows aggressively at the top of the movement.\n" +
                        "Keep your wrists straight to reduce unnecessary strain on the joints.\n" +
                        "Breathe correctly by inhaling as you lower the weight and exhaling as you press upward.\n" +
                        "Do not use too much weight, because poor form can increase shoulder injury risk.\n" +
                        "Keep your shoulder blades retracted to improve stability and chest activation.",
                "chest", "incline_bench_press");

        addExerciseInternal(db, "Flat Bench Press",
                "The Flat Bench Press is a compound exercise used to build chest strength and muscle mass.\n" +
                        "It mainly targets the pectoral muscles while also working the triceps and front shoulders.\n" +
                        "Lie flat on the bench with your eyes under the bar and your feet firmly on the floor.\n" +
                        "Lower the bar in a controlled way toward the middle of your chest.\n" +
                        "Avoid flaring your elbows too much, because this can place extra stress on the shoulders.\n" +
                        "Push the bar upward smoothly while keeping your body tight and stable.\n" +
                        "Maintain a natural arch in your back and keep your shoulder blades pulled back.\n" +
                        "Use a spotter when lifting heavy weights to improve safety.\n" +
                        "Focus on correct technique before trying to lift heavier weights.",
                "chest", "flat_bench_press");

        addExerciseInternal(db, "Chest Fly",
                "The Chest Fly is an isolation exercise that focuses mainly on the chest muscles.\n" +
                        "It can be performed with dumbbells or cables depending on the available equipment.\n" +
                        "Keep a slight bend in your elbows throughout the movement to protect the joints.\n" +
                        "Open your arms slowly until you feel a controlled stretch across the chest.\n" +
                        "Avoid lowering the weights too far, because this can stress the shoulders.\n" +
                        "Bring the weights back together by squeezing the chest muscles at the top.\n" +
                        "Do not turn the movement into a press by bending your elbows too much.\n" +
                        "Use lighter weights and controlled motion for better muscle activation.\n" +
                        "Keep your posture stable and breathe properly during each repetition.",
                "chest", "chest_fly");

        // Arms
        addExerciseInternal(db, "Bicep Curl",
                "The Bicep Curl is an isolation exercise that strengthens and develops the biceps.\n" +
                        "Stand upright with your elbows close to your torso throughout the movement.\n" +
                        "Curl the weight upward while keeping your upper arms mostly still.\n" +
                        "Avoid swinging your body or using momentum to lift the weight.\n" +
                        "Squeeze the biceps briefly at the top of the movement for better activation.\n" +
                        "Lower the weight slowly to keep tension on the muscle and improve control.\n" +
                        "Keep your wrists neutral to avoid unnecessary joint strain.\n" +
                        "Choose a weight that allows a full range of motion with proper form.\n" +
                        "Controlled repetitions are more effective than fast and careless movement.",
                "arm", "bicep_curl");

        addExerciseInternal(db, "Tricep Dips",
                "Tricep Dips are a bodyweight exercise that mainly targets the triceps.\n" +
                        "They can be performed on parallel bars, a bench, or a dip station.\n" +
                        "Keep your chest lifted and your elbows pointing backward during the movement.\n" +
                        "Lower your body slowly until your elbows reach about a 90 degree angle.\n" +
                        "Avoid going too low, because this may place extra stress on the shoulders.\n" +
                        "Push yourself back up by using your triceps while keeping control.\n" +
                        "Stay more upright if you want to focus more on the triceps than the chest.\n" +
                        "Beginners can use assisted variations to build strength safely.\n" +
                        "Move slowly and avoid bouncing at the bottom of the exercise.",
                "arm", "tricep_dips");

        // Legs
        addExerciseInternal(db, "Squats",
                "Squats are a compound lower body exercise that builds strength and stability.\n" +
                        "They mainly target the quadriceps, glutes, and hamstrings while also engaging the core.\n" +
                        "Stand with your feet about shoulder-width apart and keep your chest upright.\n" +
                        "Lower your hips back and down while keeping your knees aligned with your toes.\n" +
                        "Avoid letting your knees collapse inward during the movement.\n" +
                        "Keep your spine neutral and your core engaged throughout the exercise.\n" +
                        "Push through your heels to return to the standing position with control.\n" +
                        "Do not lean too far forward, because this can stress the lower back.\n" +
                        "Use proper warm-up and correct form before increasing the weight.",
                "legs", "squats");

        addExerciseInternal(db, "Leg Press",
                "The Leg Press is a machine exercise used to strengthen the legs safely and effectively.\n" +
                        "It mainly targets the quadriceps, glutes, and hamstrings depending on foot placement.\n" +
                        "Sit firmly against the backrest with your feet shoulder-width apart on the platform.\n" +
                        "Lower the platform slowly until your knees form about a 90 degree angle.\n" +
                        "Avoid bringing your knees too close to your chest to protect your lower back.\n" +
                        "Push the platform upward through your heels without fully locking your knees.\n" +
                        "Keep your lower back pressed against the seat during the whole movement.\n" +
                        "Use controlled motion instead of bouncing or relying on momentum.\n" +
                        "Increase the weight gradually only when your technique remains stable.",
                "legs", "leg_press");

        // Shoulders
        addExerciseInternal(db, "Overhead Press",
                "The Overhead Press is a compound exercise for building shoulder and upper body strength.\n" +
                        "It mainly targets the deltoids while also engaging the triceps and core muscles.\n" +
                        "Stand with your feet shoulder-width apart to create a stable base.\n" +
                        "Press the weight overhead in a straight path while keeping your spine neutral.\n" +
                        "Avoid excessive arching of the lower back during the lift.\n" +
                        "Lower the weight slowly back to shoulder level with full control.\n" +
                        "Keep your core tight to improve balance and protect your spine.\n" +
                        "Start with manageable weight until you learn the correct technique.\n" +
                        "Use steady breathing and proper posture during every repetition.",
                "shoulders", "overhead_press");

        addExerciseInternal(db, "Lateral Raise",
                "The Lateral Raise is an isolation exercise that targets the side deltoids.\n" +
                        "It helps develop shoulder width and improves upper body appearance.\n" +
                        "Hold the dumbbells at your sides with a slight bend in your elbows.\n" +
                        "Raise your arms outward until they reach about shoulder height.\n" +
                        "Avoid lifting the weights too high, because this can reduce shoulder efficiency.\n" +
                        "Do not swing your body or use momentum to raise the dumbbells.\n" +
                        "Lower the weights slowly to keep tension on the shoulder muscles.\n" +
                        "Use lighter weights if needed to maintain strict and controlled form.\n" +
                        "Keep your shoulders relaxed and avoid shrugging during the movement.",
                "shoulders", "lateral_raise");

        // Back
        addExerciseInternal(db, "Pull Ups",
                "Pull Ups are a challenging bodyweight exercise that strengthens the back and arms.\n" +
                        "They mainly target the latissimus dorsi while also working the biceps and upper back.\n" +
                        "Grip the bar firmly with your hands slightly wider than shoulder-width apart.\n" +
                        "Pull your body upward until your chin reaches or passes the bar.\n" +
                        "Avoid swinging your body or using momentum to complete the movement.\n" +
                        "Lower yourself slowly to maintain control and muscle tension.\n" +
                        "Keep your core engaged to stabilize your body throughout the exercise.\n" +
                        "Beginners can use resistance bands or an assisted pull-up machine.\n" +
                        "Focus on proper shoulder positioning to reduce joint stress.",
                "back", "pull_ups");

        addExerciseInternal(db, "Deadlift",
                "The Deadlift is a powerful compound exercise that builds total body strength.\n" +
                        "It mainly targets the back, glutes, hamstrings, and core muscles.\n" +
                        "Stand with your feet about hip-width apart and keep the bar close to your body.\n" +
                        "Maintain a neutral spine throughout the entire lift to protect your lower back.\n" +
                        "Lift the weight by driving through your legs and extending your hips together.\n" +
                        "Avoid rounding your back, because this greatly increases injury risk.\n" +
                        "Stand tall at the top without leaning backward excessively.\n" +
                        "Lower the bar with control instead of dropping it carelessly.\n" +
                        "Always prioritize technique over lifting the heaviest possible weight.",
                "back", "deadlift");

        // Abs
        addExerciseInternal(db, "Crunches",
                "Crunches are a basic abdominal exercise used to strengthen the core muscles.\n" +
                        "Lie on your back with your knees bent and your feet flat on the floor.\n" +
                        "Place your hands lightly behind your head or across your chest.\n" +
                        "Lift your shoulders slightly off the floor by contracting your abdominal muscles.\n" +
                        "Avoid pulling your neck forward, because this can cause discomfort or strain.\n" +
                        "Move slowly and avoid using momentum during the exercise.\n" +
                        "Exhale as you crunch upward and inhale as you return down.\n" +
                        "Keep your lower back stable and focus on quality repetitions.\n" +
                        "Correct posture is more important than doing many fast repetitions.",
                "abs", "crunches");

        addExerciseInternal(db, "Plank",
                "The Plank is an isometric exercise that strengthens the core and improves stability.\n" +
                        "It also engages the shoulders, glutes, and lower back muscles.\n" +
                        "Place your forearms or hands on the floor and keep your body in a straight line.\n" +
                        "Engage your abdominal muscles and glutes to maintain proper alignment.\n" +
                        "Avoid letting your hips sag or rise too high during the hold.\n" +
                        "Keep your neck neutral and look slightly down toward the floor.\n" +
                        "Breathe steadily instead of holding your breath during the exercise.\n" +
                        "Start with shorter holds and increase the duration as your strength improves.\n" +
                        "Perfect form is more important than holding the plank for a very long time.",
                "abs", "plank");
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