package com.example.muscleapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 30;
    private static final String DATABASE_NAME = "exerciseDB.db";
    
    public static final String TABLE_EXERCISES = "exercises";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_MUSCLE_GROUP = "muscle_group";
    public static final String COLUMN_IMAGE_NAME = "image_name";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_TAGS = "tags";

    public static final String TABLE_PROGRAMS = "user_programs";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PROGRAM_DATA = "program_json";

    public ExerciseDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EXERCISES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_MUSCLE_GROUP + " TEXT,"
                + COLUMN_IMAGE_NAME + " TEXT,"
                + COLUMN_LANGUAGE + " TEXT,"
                + COLUMN_TAGS + " TEXT" + ")");

        db.execSQL("CREATE TABLE " + TABLE_PROGRAMS + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                + COLUMN_PROGRAM_DATA + " TEXT" + ")");

        insertDefaultExercises(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAMS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void saveLocalProgram(String userId, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_PROGRAM_DATA, json);
        db.replace(TABLE_PROGRAMS, null, values);
        db.close();
    }

    public String getLocalProgram(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROGRAMS, new String[]{COLUMN_PROGRAM_DATA},
                COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, null);
        String json = null;
        if (cursor.moveToFirst()) json = cursor.getString(0);
        cursor.close();
        db.close();
        return json;
    }

    public void deleteLocalProgram(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROGRAMS, COLUMN_USER_ID + " = ?", new String[]{userId});
        db.close();
    }

    public List<Exercise> getExercisesByMuscleGroup(String muscleGroup, String lang) {
        List<Exercise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "(" + COLUMN_MUSCLE_GROUP + " = ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ?) AND " +
                COLUMN_LANGUAGE + " = ?";
        String[] selectionArgs = new String[]{muscleGroup, muscleGroup + ",%", "%," + muscleGroup, "%," + muscleGroup + ",%", lang};
        Cursor cursor = db.query(TABLE_EXERCISES, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Exercise(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(6)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Exercise getExerciseByImageName(String imageName, String lang) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES, null, COLUMN_IMAGE_NAME + " = ? AND " + COLUMN_LANGUAGE + " = ?", new String[]{imageName, lang}, null, null, null);
        Exercise exercise = null;
        if (cursor.moveToFirst()) {
            exercise = new Exercise(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(6));
        }
        cursor.close();
        db.close();
        return exercise;
    }

    public void addExercise(String title, String description, String muscleGroup, String imageName, String lang, String tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_MUSCLE_GROUP, muscleGroup);
        values.put(COLUMN_IMAGE_NAME, imageName);
        values.put(COLUMN_LANGUAGE, lang);
        values.put(COLUMN_TAGS, tags);
        db.insert(TABLE_EXERCISES, null, values);
        db.close();
    }

    public void deleteExerciseByImageName(String imageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISES, COLUMN_IMAGE_NAME + " = ?", new String[]{imageName});
        db.close();
    }

    private void insertDefaultExercises(SQLiteDatabase db) {
        insertEnglishExercises(db);
        insertGreekExercises(db);
    }

    private void insertEnglishExercises(SQLiteDatabase db) {
        // --- CHEST (10) ---
        addExerciseInternal(db, "Incline Bench Press", "1. Adjust bench to 30-45 degrees.\n2. Keep feet flat on floor.\n3. Retract shoulder blades.\n4. Lower bar to upper chest.\n5. Control the descent.\n6. Drive weight up explosively.\n7. Exhale on the push.\n8. Don't bounce bar off chest.\n9. Keep wrists straight.\n10. Targets upper pectorals.", "chest", "incline_bench_press", "en", "upper chest,compound");
        addExerciseInternal(db, "Flat Bench Press", "1. Lie flat on the bench.\n2. Grip bar slightly wider than shoulders.\n3. Plant feet firmly for stability.\n4. Lower bar slowly to mid-chest.\n5. Keep elbows at 45 degrees.\n6. Push up until arms are straight.\n7. Focus on chest contraction.\n8. Maintain natural back arch.\n9. Do not lock elbows fully.\n10. Primary mass builder for chest.", "chest", "flat_bench_press", "en", "middle chest,compound");
        addExerciseInternal(db, "Chest Fly", "1. Use dumbbells or cable machine.\n2. Keep a slight bend in elbows.\n3. Open arms wide for deep stretch.\n4. Don't lower weights below shoulders.\n5. Squeeze chest to bring weights together.\n6. Imagine hugging a large tree.\n7. Keep core tight for stability.\n8. Use controlled, slow motion.\n9. Great for pectoral isolation.\n10. Avoid using too much weight.", "chest", "chest_fly", "en", "inner chest,isolation");
        addExerciseInternal(db, "Push Ups", "1. Start in a high plank position.\n2. Place hands under shoulders.\n3. Keep body in a straight line.\n4. Engage core and glutes.\n5. Lower chest towards the floor.\n6. Keep elbows tucked slightly back.\n7. Push back to starting position.\n8. Breathe in down, out up.\n9. Can be done on knees if needed.\n10. Functional bodyweight movement.", "chest", "push_ups", "en", "chest,bodyweight");
        addExerciseInternal(db, "Decline Bench Press", "1. Use a decline bench at 15-30 deg.\n2. Secure feet under foot pads.\n3. Head should be lower than hips.\n4. Lower bar to lower chest.\n5. Use a wide, stable grip.\n6. Push weight up vertically.\n7. Focus on the lower pec fibers.\n8. Keep shoulders protected.\n9. Controlled reps are essential.\n10. Enhances chest definition.", "chest", "decline_bench_press", "en", "lower chest,compound");
        addExerciseInternal(db, "Cable Crossover", "1. Set pulleys to high position.\n2. Stand centrally with one foot forward.\n3. Lean forward slightly at hips.\n4. Pull cables down and forward.\n5. Squeeze hands together in front.\n6. Feel the peak contraction.\n7. Slow return for max tension.\n8. Keep elbows slightly bent.\n9. Great for muscle separation.\n10. Focus on the squeeze.", "chest", "cable_crossover", "en", "inner chest,isolation");
        addExerciseInternal(db, "Dumbbell Pullover", "1. Lie across a flat bench.\n2. Only shoulders touch the bench.\n3. Hold one dumbbell with both hands.\n4. Start with weight over chest.\n5. Lower dumbbell behind head.\n6. Feel deep stretch in ribcage.\n7. Pull weight back to center.\n8. Squeeze pecs and lats.\n9. Maintain slight elbow bend.\n10. Improves posture and chest width.", "chest", "dumbbell_pullover", "en", "chest,lats,isolation");
        addExerciseInternal(db, "Dumbbell Bench Press", "1. Lie flat on bench.\n2. Hold dumbbells over chest.\n3. Lower weights to chest level.\n4. Keep elbows at 45 degrees.\n5. Press dumbbells up.\n6. Squeeze chest at top.\n7. Control the movement.\n8. Maintain stable core.\n9. Inhale down, exhale up.\n10. Allows deeper range.", "chest", "db_bench_press", "en", "chest,compound");
        addExerciseInternal(db, "Chest Press Machine", "1. Sit with back against pad.\n2. Adjust handles to chest height.\n3. Feet flat on floor.\n4. Push handles forward.\n5. Don't lock elbows.\n6. Return slowly.\n7. Keep shoulders back.\n8. Focus on chest contraction.\n9. Constant tension.\n10. Safe for heavy lifting.", "chest", "chest_press_machine", "en", "chest,compound");
        addExerciseInternal(db, "Pec Deck Fly", "1. Sit with back flat.\n2. Forearms on the pads.\n3. Squeeze handles together.\n4. Feel inner chest contract.\n5. Hold for 1 second.\n6. Open arms slowly.\n7. Don't go past shoulders.\n8. Keep head neutral.\n9. Control the return.\n10. Pure chest isolation.", "chest", "pec_deck", "en", "chest,isolation");

        // --- ARMS (10) ---
        addExerciseInternal(db, "Bicep Curl", "1. Stand upright with weights.\n2. Keep elbows close to torso.\n3. Curl weights toward shoulders.\n4. Only move your lower arms.\n5. Squeeze biceps at the top.\n6. Lower weights with control.\n7. Avoid swinging the body.\n8. Keep wrists in neutral position.\n9. Full range of motion is key.\n10. Classic bicep builder.", "arm", "bicep_curl", "en", "biceps,isolation");
        addExerciseInternal(db, "Tricep Dips", "1. Use parallel bars or bench.\n2. Keep hands shoulder-width apart.\n3. Lower body by bending elbows.\n4. Keep chest up and shoulders back.\n5. Go down until 90 degree bend.\n6. Push up using triceps power.\n7. Lean forward for chest focus.\n8. Stay upright for tricep focus.\n9. Use assist machine if needed.\n10. Effective compound arm move.", "arm,chest", "tricep_dips", "en", "triceps,compound");
        addExerciseInternal(db, "Hammer Curl", "1. Hold dumbbells with palms facing in.\n2. Stand with feet shoulder-width.\n3. Curl weights like a hammer.\n4. Targets the brachialis muscle.\n5. Helps build arm thickness.\n6. Control the eccentric phase.\n7. Keep torso perfectly still.\n8. Engage core for stability.\n9. Alternate arms or curl both.\n10. Reduces wrist strain.", "arm", "hammer_curl", "en", "biceps,forearms,isolation");
        addExerciseInternal(db, "Tricep Pushdown", "1. Use cable machine with bar or rope.\n2. Elbows glued to your sides.\n3. Push bar down until arms straight.\n4. Squeeze triceps at the bottom.\n5. Slowly return to start.\n6. Don't let elbows flare out.\n7. Lean slightly forward.\n8. Constant tension from cables.\n9. High reps for better pump.\n10. Pure tricep isolation.", "arm", "tricep_pushdown", "en", "triceps,isolation");
        addExerciseInternal(db, "Preacher Curl", "1. Sit at preacher bench station.\n2. Place upper arms on the pad.\n3. Grip EZ bar or dumbbells.\n4. Lower weight fully for stretch.\n5. Curl weight up to shoulders.\n6. Squeeze hard at peak.\n7. Pad prevents body swinging.\n8. Focuses on short bicep head.\n9. Great for bicep peaks.\n10. Maintain strict form.", "arm", "preacher_curl", "en", "biceps,isolation");
        addExerciseInternal(db, "Skull Crushers", "1. Lie on flat bench with EZ bar.\n2. Extend arms above shoulders.\n3. Lower bar toward forehead.\n4. Only bend at the elbows.\n5. Stop just before touching head.\n6. Extend bar back to start.\n7. Keep upper arms stationary.\n8. Intense tricep stretch.\n9. Use a spotter for safety.\n10. Also called French Press.", "arm", "skull_crushers", "en", "triceps,isolation");
        addExerciseInternal(db, "Concentration Curl", "1. Sit on bench, legs spread.\n2. Rest elbow on inner thigh.\n3. Curl dumbbell toward chest.\n4. Focus purely on bicep.\n5. No momentum allowed.\n6. Squeeze at top of movement.\n7. Slow, controlled return.\n8. Great for mind-muscle connection.\n9. Eliminates shoulder involvement.\n10. Finish with high reps.", "arm", "concentration_curl", "en", "biceps,isolation");
        addExerciseInternal(db, "Overhead Tricep Extension", "1. Stand or sit upright.\n2. Hold dumbbell with both hands.\n3. Raise weight over head.\n4. Lower weight behind neck.\n5. Keep elbows close to ears.\n6. Extend arms back to top.\n7. Squeeze triceps hard.\n8. Maintain vertical spine.\n9. Avoid arching back.\n10. Long head tricep focus.", "arm", "overhead_tricep_ext", "en", "triceps,isolation");
        addExerciseInternal(db, "Close Grip Bench Press", "1. Lie flat on bench.\n2. Grip bar at shoulder width.\n3. Lower bar to lower chest.\n4. Keep elbows tucked in.\n5. Push bar up explosively.\n6. Focus on tricep drive.\n7. Don't flare elbows out.\n8. Keep wrists stable.\n9. Inhale down, exhale up.\n10. Heavy tricep builder.", "arm", "close_grip_bench", "en", "triceps,chest,compound");
        addExerciseInternal(db, "Zottman Curl", "1. Stand with dumbbells.\n2. Palms up, curl weights.\n3. Rotate palms down at top.\n4. Lower weights with palms down.\n5. Rotate back to palms up.\n6. Combines bicep/forearm work.\n7. Control the rotation.\n8. Slow eccentric phase.\n9. Keep torso stationary.\n10. Great for grip strength.", "arm", "zottman_curl", "en", "biceps,forearms,isolation");

        // --- LEGS (10) ---
        addExerciseInternal(db, "Squats", "1. Feet shoulder-width apart.\n2. Bar on upper traps or shoulders.\n3. Chest up, back straight.\n4. Break at hips and knees.\n5. Lower until thighs are parallel.\n6. Push through heels to rise.\n7. Keep knees aligned with toes.\n8. Inhale down, exhale up.\n9. Fundamental lower body power.\n10. Targets quads, glutes, core.", "legs", "squats", "en", "quads,glutes,compound");
        addExerciseInternal(db, "Leg Press", "1. Sit firmly in the machine.\n2. Place feet on platform.\n3. Unlock safety handles.\n4. Lower platform to 90 degrees.\n5. Don't let lower back lift.\n6. Push platform up with heels.\n7. Do not lock out knees.\n8. Control the weight carefully.\n9. Adjust feet for different focus.\n10. Safe heavy leg training.", "legs", "leg_press", "en", "quads,glutes,compound");
        addExerciseInternal(db, "Lunges", "1. Stand tall, feet together.\n2. Step forward with one leg.\n3. Lower hips until knees are 90 deg.\n4. Rear knee almost touches floor.\n5. Keep front knee over ankle.\n6. Push back to starting position.\n7. Switch legs and repeat.\n8. Maintains balance and stability.\n9. Targets glutes and quads.\n10. Can be done with dumbbells.", "legs", "lunges", "en", "quads,glutes,unilateral");
        addExerciseInternal(db, "Leg Extension", "1. Sit on machine, back against pad.\n2. Place shins under the bar.\n3. Extend legs until straight.\n4. Hold contraction for 1 second.\n5. Lower slowly to start.\n6. Do not swing the weight.\n7. Grip side handles for support.\n8. Isolates the quadriceps.\n9. Perfect for muscle definition.\n10. Use higher rep ranges.", "legs", "leg_extension", "en", "quads,isolation");
        addExerciseInternal(db, "Leg Curl", "1. Lie face down on machine.\n2. Align knees with pivot point.\n3. Curl bar toward glutes.\n4. Squeeze hamstrings hard.\n5. Return weight slowly.\n6. Keep hips on the pad.\n7. Focus on hamstring contraction.\n8. Build posterior leg strength.\n9. Essential for leg balance.\n10. Prevents knee injuries.", "legs", "leg_curl", "en", "hamstrings,isolation");
        addExerciseInternal(db, "Calf Raises", "1. Stand on edge of platform.\n2. Only balls of feet on surface.\n3. Lower heels for deep stretch.\n4. Push up onto tiptoes.\n5. Squeeze calves at the top.\n6. Hold for a brief pause.\n7. Lower slowly and repeat.\n8. Use standing or seated machine.\n9. High reps work best.\n10. Targets gastrocnemius.", "legs", "calf_raises", "en", "calves,isolation");
        addExerciseInternal(db, "Glute Bridge", "1. Lie on back, knees bent.\n2. Feet flat on the floor.\n3. Lift hips toward ceiling.\n4. Squeeze glutes at the top.\n5. Keep core engaged.\n6. Lower hips slowly.\n7. Use barbell for extra weight.\n8. Great for hip stability.\n9. Prime glute activation.\n10. Protects lower back.", "legs", "glute_bridge", "en", "glutes,isolation");
        addExerciseInternal(db, "Romanian Deadlift", "1. Stand with feet hip-width.\n2. Hold bar in front of thighs.\n3. Hinge at hips, back flat.\n4. Lower bar along shins.\n5. Go until hamstrings stretch.\n6. Drive hips forward to rise.\n7. Squeeze glutes at top.\n8. Keep knees slightly bent.\n9. Don't round lower back.\n10. Prime hamstring movement.", "legs", "rdl", "en", "hamstrings,glutes,compound");
        addExerciseInternal(db, "Step Ups", "1. Stand before a bench/box.\n2. Place one foot on surface.\n3. Drive through front heel.\n4. Step up until leg straight.\n5. Bring other foot up.\n6. Step down with control.\n7. Switch legs or repeat.\n8. Keep torso upright.\n9. Engage core for balance.\n10. Unilateral leg power.", "legs", "step_ups", "en", "quads,glutes,unilateral");
        addExerciseInternal(db, "Goblet Squat", "1. Hold dumbbell at chest.\n2. Feet slightly wider than shoulders.\n3. Squat down between knees.\n4. Keep chest up, back flat.\n5. Elbows touch inner thighs.\n6. Push back up to start.\n7. Drive through entire foot.\n8. Maintain core tension.\n9. Inhale down, exhale up.\n10. Perfect for squat depth.", "legs", "goblet_squat", "en", "quads,glutes,compound");

        // --- SHOULDERS (10) ---
        addExerciseInternal(db, "Overhead Press", "1. Stand with feet shoulder-width.\n2. Bar at upper chest level.\n3. Press bar vertically overhead.\n4. Keep core tight, no arching.\n5. Lock out arms at top.\n6. Lower bar back to shoulders.\n7. Move head out of bar path.\n8. Full shoulder development.\n9. High stability required.\n10. Military style power.", "shoulders", "overhead_press", "en", "shoulders,compound");
        addExerciseInternal(db, "Lateral Raise", "1. Stand with dumbbells at sides.\n2. Slight bend in elbows.\n3. Raise arms out to sides.\n4. Stop at shoulder height.\n5. Lower with control.\n6. Do not use momentum.\n7. Focus on side deltoids.\n8. Creates shoulder width.\n9. Pinkies up for more focus.\n10. Isolation movement.", "shoulders", "lateral_raise", "en", "side delts,isolation");
        addExerciseInternal(db, "Front Raise", "1. Hold dumbbells in front.\n2. Lift one arm at a time.\n3. Raise to shoulder level.\n4. Keep arm nearly straight.\n5. Control the descent.\n6. Targets anterior deltoids.\n7. Alternate arms for focus.\n8. Don't swing your body.\n9. Use light to moderate weight.\n10. Anterior shoulder detail.", "shoulders", "front_raise", "en", "front delts,isolation");
        addExerciseInternal(db, "Face Pulls", "1. Use cable rope at eye level.\n2. Pull rope toward your face.\n3. Pull ends of rope apart.\n4. Squeeze rear delts and traps.\n5. External rotation at the end.\n6. Slow return for tension.\n7. Great for shoulder health.\n8. Corrects rounded posture.\n9. High reps recommended.\n10. Posterior chain focus.", "shoulders", "face_pulls", "en", "rear delts,traps");
        addExerciseInternal(db, "Shrugs", "1. Hold heavy dumbbells or bar.\n2. Arms straight at your sides.\n3. Lift shoulders toward ears.\n4. Squeeze traps at the top.\n5. Hold for a second.\n6. Lower slowly.\n7. Don't roll your shoulders.\n8. Use heavy weights safely.\n9. Build neck and trap mass.\n10. Simple but effective.", "shoulders", "shrugs", "en", "traps,isolation");
        addExerciseInternal(db, "Reverse Fly", "1. Bend forward at hips.\n2. Keep back flat, core tight.\n3. Raise weights out to sides.\n4. Squeeze shoulder blades.\n5. Targets posterior deltoids.\n6. Slow, precise motion.\n7. Keep head in neutral line.\n8. Don't use heavy swinging.\n9. Essential for back balance.\n10. Improves rear shoulder look.", "shoulders", "reverse_fly", "en", "rear delts,isolation");
        addExerciseInternal(db, "Arnold Press", "1. Sit with dumbbells at chest.\n2. Palms facing toward you.\n3. Rotate palms as you press.\n4. Finish with palms facing out.\n5. Reverse rotation on way down.\n6. Full range of motion.\n7. Named after Arnold Schwarzenegger.\n8. Targets all delt heads.\n9. Keep core engaged.\n10. Advanced pressing move.", "shoulders", "arnold_press", "en", "shoulders,compound");
        addExerciseInternal(db, "Upright Row", "1. Stand with bar/dumbbells.\n2. Grip slightly narrower than shoulders.\n3. Pull weights toward chin.\n4. Lead with the elbows.\n5. Keep weights close to body.\n6. Pause at the top.\n7. Lower slowly to start.\n8. Don't rock the body.\n9. Focus on side delts/traps.\n10. Controlled vertical pull.", "shoulders", "upright_row", "en", "shoulders,traps,compound");
        addExerciseInternal(db, "Military Press", "1. Stand with feet together/narrow.\n2. Bar on upper chest.\n3. Press bar straight up.\n4. Head moves back then forward.\n5. Lock out arms at top.\n6. Avoid leg assistance.\n7. Keep glutes squeezed tight.\n8. Exhale on the press.\n9. Control the descent.\n10. Strict overhead strength.", "shoulders", "military_press", "en", "shoulders,compound");
        addExerciseInternal(db, "Cable Lateral Raise", "1. Stand side to cable machine.\n2. Handle in far hand.\n3. Raise arm out to side.\n4. Maintain constant cable tension.\n5. Stop at shoulder height.\n6. Return slowly across body.\n7. Keep core stabilized.\n8. Avoid using momentum.\n9. Isolates medial deltoid.\n10. Better tension than dumbbells.", "shoulders", "cable_lateral_raise", "en", "shoulders,isolation");

        // --- BACK (10) ---
        addExerciseInternal(db, "Pull Ups", "1. Grip bar wider than shoulders.\n2. Hang with arms fully extended.\n3. Pull chest toward the bar.\n4. Squeeze lats at the top.\n5. Chin should pass the bar.\n6. Lower yourself slowly.\n7. Avoid using leg momentum.\n8. Engage core throughout.\n9. King of back exercises.\n10. Build width and strength.", "back", "pull_ups", "en", "lats,compound");
        addExerciseInternal(db, "Deadlift", "1. Feet hip-width under bar.\n2. Flat back, grip the bar.\n3. Drive through your legs.\n4. Pull bar close to shins.\n5. Stand tall, lock hips.\n6. Control descent to floor.\n7. Maintain neutral spine.\n8. Total body strength builder.\n9. Heavy weight recommended.\n10. Focus on technique first.", "back,legs", "deadlift", "en", "back,legs,compound");
        addExerciseInternal(db, "Bent Over Row", "1. Bend at hips, back flat.\n2. Grip barbell with overhand grip.\n3. Pull bar toward lower stomach.\n4. Squeeze back muscles hard.\n5. Lower bar with control.\n6. Keep core very tight.\n7. Build back thickness.\n8. Don't use momentum.\n9. Protect your lower back.\n10. Effective row variant.", "back", "bent_over_row", "en", "back,compound");
        addExerciseInternal(db, "Lat Pulldown", "1. Sit at cable machine.\n2. Grip wide bar, palms out.\n3. Pull bar to upper chest.\n4. Arch back slightly.\n5. Squeeze lats at bottom.\n6. Slowly release bar up.\n7. Control the stretch.\n8. Substitute for pull ups.\n9. Targets upper back width.\n10. Adjustable resistance.", "back", "lat_pulldown", "en", "lats,isolation");
        addExerciseInternal(db, "Seated Cable Row", "1. Sit with feet on pads.\n2. Slight bend in knees.\n3. Pull handle toward navel.\n4. Pull shoulders back.\n5. Squeeze blades together.\n6. Extend arms fully for stretch.\n7. Keep torso stationary.\n8. Thickens the middle back.\n9. Constant cable tension.\n10. Versatile grip options.", "back", "seated_cable_row", "en", "back,compound");
        addExerciseInternal(db, "T-Bar Row", "1. Straddle the T-bar landmine.\n2. Use a V-handle grip.\n3. Pull bar toward chest.\n4. Squeeze mid-back and traps.\n5. Lower bar back to floor.\n6. Maintain a flat back.\n7. Old-school mass builder.\n8. Intense back isolation.\n9. Brace core strongly.\n10. Great for back power.", "back", "t_bar_row", "en", "back,compound");
        addExerciseInternal(db, "Dumbbell Row", "1. One hand and knee on bench.\n2. Flat back, other arm hangs.\n3. Pull dumbbell to hip.\n4. Squeeze lats and traps.\n5. Lower with full stretch.\n6. Work one side at a time.\n7. Improves muscle balance.\n8. Deep range of motion.\n9. Don't twist your torso.\n10. Effective isolation row.", "back", "dumbbell_row", "en", "back,unilateral");
        addExerciseInternal(db, "Hyperextensions", "1. Position yourself on bench.\n2. Ankles under foot pads.\n3. Bend forward at hips.\n4. Lower torso toward floor.\n5. Raise up until body straight.\n6. Squeeze lower back/glutes.\n7. Don't overextend at top.\n8. Keep hands behind head.\n9. Slow, rhythmic motion.\n10. Essential for lower back.", "back", "hyperextensions", "en", "back,compound");
        addExerciseInternal(db, "Close Grip Lat Pulldown", "1. Use V-bar attachment.\n2. Sit and secure legs.\n3. Lean back slightly.\n4. Pull handle to chest.\n5. Squeeze lats and mid-back.\n6. Extend arms fully up.\n7. Control the weight.\n8. Don't use body weight.\n9. Great for lower lats.\n10. Improves back thickness.", "back", "close_grip_pulldown", "en", "back,lats,compound");
        addExerciseInternal(db, "Single Arm Lat Row", "1. Stand at cable machine.\n2. One knee on floor.\n3. Pull handle toward hip.\n4. Rotate palm slightly.\n5. Feel the lat contract.\n6. Full stretch at start.\n7. Keep shoulders square.\n8. Work each side separately.\n9. Eliminates imbalances.\n10. Focused back isolation.", "back", "single_arm_row", "en", "back,unilateral");

        // --- ABS (10) ---
        addExerciseInternal(db, "Crunches", "1. Lie on back, knees bent.\n2. Hands behind head lightly.\n3. Lift shoulders off floor.\n4. Squeeze abs at top.\n5. Exhale as you rise.\n6. Lower back stays on floor.\n7. Quality over quantity.\n8. Don't pull on your neck.\n9. Target upper abs.\n10. Classic core move.", "abs", "crunches", "en", "abs,isolation");
        addExerciseInternal(db, "Plank", "1. Rest on forearms and toes.\n2. Body in a straight line.\n3. Squeeze glutes and abs.\n4. Don't let hips sag.\n5. Keep neck neutral.\n6. Breathe steadily.\n7. Hold for max time.\n8. Builds core endurance.\n9. Isometric stability.\n10. No equipment needed.", "abs", "plank", "en", "core,stability");
        addExerciseInternal(db, "Leg Raises", "1. Lie flat, hands at sides.\n2. Keep legs straight.\n3. Lift legs to 90 degrees.\n4. Lower slowly, don't touch floor.\n5. Control the movement.\n6. Press lower back down.\n7. Targets lower abs.\n8. Can do hanging version.\n9. Maintain constant tension.\n10. Effective for lower core.", "abs", "leg_raises", "en", "abs,isolation");
        addExerciseInternal(db, "Russian Twist", "1. Sit with knees bent.\n2. Lean back slightly.\n3. Rotate torso side to side.\n4. Touch floor with hands.\n5. Keep feet off floor for challenge.\n6. Focus on obliques.\n7. Control the rotation.\n8. Use a weight for intensity.\n9. Improves rotational power.\n10. Great for side abs.", "abs", "russian_twist", "en", "obliques,isolation");
        addExerciseInternal(db, "Bicycle Crunches", "1. Lie on back, legs up.\n2. Elbow to opposite knee.\n3. Alternate in pedaling motion.\n4. Full torso rotation.\n5. Squeeze abs hard.\n6. Keep speed controlled.\n7. Targets whole core.\n8. Effective oblique work.\n9. Burn more calories.\n10. Dynamic crunch variant.", "abs", "bicycle_crunches", "en", "abs,obliques");
        addExerciseInternal(db, "Mountain Climbers", "1. High plank position.\n2. Drive knees to chest fast.\n3. Alternate legs quickly.\n4. Keep hips low.\n5. Engage core throughout.\n6. Cardio and core combined.\n7. Rapid fire movement.\n8. Build functional speed.\n9. Burn fat while building abs.\n10. High intensity move.", "abs", "mountain_climbers", "en", "core,cardio");
        addExerciseInternal(db, "Hanging Knee Raise", "1. Hang from pull up bar.\n2. Bring knees to chest.\n3. Squeeze abs at top.\n4. Lower legs slowly.\n5. Don't swing your body.\n6. Advanced core move.\n7. Targets lower abs primarily.\n8. Grip strength required.\n9. Strict form is vital.\n10. Powerful ab builder.", "abs", "hanging_knee_raise", "en", "abs,isolation");
        addExerciseInternal(db, "Flutter Kicks", "1. Lie on your back.\n2. Hands under your glutes.\n3. Lift legs 6 inches off floor.\n4. Alternate kicking legs up/down.\n5. Keep legs straight.\n6. Don't touch the floor.\n7. Squeeze lower abs tight.\n8. Maintain small, fast kicks.\n9. Breathe naturally.\n10. Intense lower core burner.", "abs", "flutter_kicks", "en", "abs,isolation");
        addExerciseInternal(db, "Dead Bug", "1. Lie on back, limbs up.\n2. Knees bent at 90 deg.\n3. Lower opposite arm/leg.\n4. Keep lower back on floor.\n5. Return to start position.\n6. Switch to other sides.\n7. Move very slowly.\n8. Focus on core stability.\n9. Don't let back arch.\n10. Safe for spine health.", "abs", "dead_bug", "en", "abs,stability");
        addExerciseInternal(db, "Plank with Hip Dips", "1. Start in forearm plank.\n2. Body in straight line.\n3. Rotate hips to one side.\n4. Tap floor with hip.\n5. Rotate to other side.\n6. Keep core very tight.\n7. Move in controlled arc.\n8. Don't sag the middle.\n9. Engages obliques/core.\n10. Dynamic plank variant.", "abs", "plank_hip_dips", "en", "abs,obliques");
    }

    private void insertGreekExercises(SQLiteDatabase db) {
        // --- ΣΤΗΘΟΣ (10) ---
        addExerciseInternal(db, "Πιέσεις Πάγκου με Κλίση", "1. Ρυθμίστε τον πάγκο 30-45 μοίρες.\n2. Πόδια σταθερά στο πάτωμα.\n3. Τραβήξτε τις ωμοπλάτες πίσω.\n4. Κατεβάστε στο πάνω στήθος.\n5. Ελέγξτε την κάθοδο.\n6. Σπρώξτε εκρηκτικά.\n7. Εκπνεύστε στην ώθηση.\n8. Μην αναπηδάτε τη μπάρα.\n9. Καρποί σε ευθεία.\n10. Στοχεύει το πάνω στήθος.", "chest", "incline_bench_press", "el", "πάνω στήθος,σύνθετη");
        addExerciseInternal(db, "Πιέσεις σε Ίσιο Πάγκο", "1. Ξαπλώστε ίσια στον πάγκο.\n2. Λαβή λίγο έξω από τους ώμους.\n3. Πόδια γερά στο έδαφος.\n4. Μπάρα στο μέσο του στήθους.\n5. Αγκώνες στις 45 μοίρες.\n6. Σπρώξτε μέχρι την έκταση.\n7. Εστιάστε στη σύσπαση.\n8. Φυσική καμπύλη στη μέση.\n9. Μην κλειδώνετε απότομα.\n10. Βασική άσκηση όγκου στήθους.", "chest", "flat_bench_press", "el", "μεσαίο στήθος,σύνθετη");
        addExerciseInternal(db, "Ανοίγματα Στήθους", "1. Αλτήρες ή τροχαλία.\n2. Ελαφριά κάμψη στους αγκώνες.\n3. Ανοίξτε για βαθιά διάταση.\n4. Μην κατεβαίνετε κάτω από ώμους.\n5. Σφίξτε το στήθος στο κλείσιμο.\n6. Κίνηση σαν αγκαλιά δέντρου.\n7. Κορμός σταθερός.\n8. Ελεγχόμενη, αργή κίνηση.\n9. Απομόνωση θωρακικών.\n10. Αποφύγετε τα πολλά βάρη.", "chest", "chest_fly", "el", "εσωτερικό στήθος,απομόνωση");
        addExerciseInternal(db, "Κάμψεις (Push Ups)", "1. Θέση υψηλής σανίδας.\n2. Χέρια κάτω από τους ώμους.\n3. Σώμα σε ευθεία γραμμή.\n4. Ενεργοποιήστε κοιλιά-γλουτούς.\n5. Στήθος προς το πάτωμα.\n6. Αγκώνες λίγο προς τα πίσω.\n7. Σπρώξτε στην αρχική θέση.\n8. Εισπνοή κάτω, εκπνοή πάνω.\n9. Και στα γόνατα αν χρειαστεί.\n10. Λειτουργική άσκηση σώματος.", "chest", "push_ups", "el", "στήθος,σωματικό βάρος");
        addExerciseInternal(db, "Πιέσεις σε Κατακλινή Πάγκο", "1. Πάγκος με κλίση 15-30 μοίρες.\n2. Ασφαλίστε τα πόδια.\n3. Κεφάλι χαμηλότερα από γοφούς.\n4. Μπάρα στο κάτω μέρος στήθους.\n5. Σταθερή, πλατιά λαβή.\n6. Σπρώξτε κάθετα πάνω.\n7. Εστίαση στις κάτω ίνες.\n8. Προστατέψτε τους ώμους.\n9. Ελεγχόμενες επαναλήψεις.\n10. Βελτιώνει το σχήμα στήθους.", "chest", "decline_bench_press", "el", "κάτω στήθος,σύνθετη");
        addExerciseInternal(db, "Cable Crossover (Διασταυρώσεις)", "1. Τροχαλίες σε υψηλή θέση.\n2. Ένα πόδι μπροστά, κέντρο.\n3. Κλίση μπροστά στη μέση.\n4. Τραβήξτε κάτω και μπροστά.\n5. Σφίξτε τα χέρια μπροστά.\n6. Νιώστε τη μέγιστη σύσπαση.\n7. Αργή επιστροφή για τάση.\n8. Αγκώνες ελαφρώς λυγισμένοι.\n9. Διαχωρισμός μυών.\n10. Εστιάστε στο σφίξιμο.", "chest", "cable_crossover", "el", "εσωτερικό στήθος,απομόνωση");
        addExerciseInternal(db, "Dumbbell Pullover (Εκτάσεις)", "1. Ξαπλώστε εγκάρσια στον πάγκο.\n2. Μόνο ώμοι ακουμπούν πάγκο.\n3. Ένας αλτήρας με δύο χέρια.\n4. Βάρος πάνω από το στήθος.\n5. Κατεβάστε πίσω από κεφάλι.\n6. Διάταση στον θώρακα.\n7. Επαναφορά στο κέντρο.\n8. Σφίξτε στήθος και φτερά.\n9. Ελαφριά κάμψη αγκώνων.\n10. Βελτιώνει στάση και εύρος.", "chest", "dumbbell_pullover", "el", "στήθος,φτερά,απομόνωση");
        addExerciseInternal(db, "Πιέσεις Πάγκου με Αλτήρες", "1. Ξαπλώστε στον πάγκο.\n2. Κρατήστε αλτήρες πάνω από το στήθος.\n3. Κατεβάστε στο ύψος του στήθους.\n4. Αγκώνες στις 45 μοίρες.\n5. Σπρώξτε τους αλτήρες πάνω.\n6. Σφίξτε το στήθος πάνω.\n7. Ελέγξτε την κίνηση.\n8. Σταθερός κορμός.\n9. Εισπνοή κάτω, εκπνοή πάνω.\n10. Μεγαλύτερο εύρος κίνησης.", "chest", "db_bench_press", "el", "στήθος,σύνθετη");
        addExerciseInternal(db, "Μηχανή Πιέσεων Στήθους", "1. Καθίστε με την πλάτη στο μαξιλάρι.\n2. Ρυθμίστε τις λαβές στο ύψος στήθους.\n3. Πόδια στο έδαφος.\n4. Σπρώξτε τις λαβές μπροστά.\n5. Μην κλειδώνετε αγκώνες.\n6. Επιστρέψτε αργά.\n7. Ώμοι πίσω.\n8. Εστίαση στη σύσπαση.\n9. Συνεχής τάση.\n10. Ασφαλές για πολλά βάρη.", "chest", "chest_press_machine", "el", "στήθος,σύνθετη");
        addExerciseInternal(db, "Pec Deck Fly", "1. Καθίστε με την πλάτη ίσια.\n2. Πήχεις στα μαξιλαράκια.\n3. Σφίξτε τις λαβές μαζί.\n4. Νιώστε το εσωτερικό στήθος.\n5. Κρατήστε για 1 δευτερόλεπτο.\n6. Ανοίξτε τα χέρια αργά.\n7. Όχι πίσω από τους ώμους.\n8. Κεφάλι σε ουδέτερη θέση.\n9. Ελεγχόμενη επιστροφή.\n10. Καθαρή απομόνωση στήθους.", "chest", "pec_deck", "el", "στήθος,απομόνωση");

        // --- ΧΕΡΙΑ (10) ---
        addExerciseInternal(db, "Κάμψεις Δικεφάλων", "1. Όρθια στάση με βάρη.\n2. Αγκώνες κολλημένοι στον κορμό.\n3. Λυγίστε προς τους ώμους.\n4. Μόνο οι πήχεις κινούνται.\n5. Σφίξτε δικέφαλους στην κορυφή.\n6. Κατεβάστε ελεγχόμενα.\n7. Μην κουνάτε το σώμα.\n8. Καρποί σε ουδέτερη θέση.\n9. Πλήρες εύρος κίνησης.\n10. Κλασική άσκηση δικεφάλων.", "arm", "bicep_curl", "el", "δικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Βυθίσεις Τρικεφάλων", "1. Δίζυγο ή πάγκος.\n2. Χέρια στο άνοιγμα ώμων.\n3. Κατεβείτε λυγίζοντας αγκώνες.\n4. Στήθος ψηλά, ώμοι πίσω.\n5. Μέχρι γωνία 90 μοιρών.\n6. Σπρώξτε με τη δύναμη τρικέφαλων.\n7. Κλίση μπροστά για στήθος.\n8. Όρθιοι για τρικέφαλους.\n9. Χρήση υποβοήθησης αν χρειαστεί.\n10. Σύνθετη άσκηση χεριών.", "arm,chest", "tricep_dips", "el", "τρικέφαλοι,σύνθετη");
        addExerciseInternal(db, "Κάμψεις Hammer", "1. Αλτήρες, παλάμες μέσα.\n2. Πόδια στο άνοιγμα ώμων.\n3. Κάμψη σαν σφυρί.\n4. Στοχεύει τον βραχιόνιο.\n5. Χτίζει το πάχος του χεριού.\n6. Ελέγξτε την κάθοδο.\n7. Κορμός απόλυτα σταθερός.\n8. Ενεργοποιήστε τον κορμό.\n9. Εναλλάξ ή και τα δύο.\n10. Λιγότερη πίεση στον καρπό.", "arm", "hammer_curl", "el", "δικέφαλοι,πήχεις,απομόνωση");
        addExerciseInternal(db, "Εκτάσεις Τρικεφάλων (Pushdown)", "1. Τροχαλία με μπάρα ή σχοινί.\n2. Αγκώνες κολλημένοι στα πλευρά.\n3. Πιέστε κάτω μέχρι την έκταση.\n4. Σφίξτε τρικέφαλους κάτω.\n5. Αργή επιστροφή πάνω.\n6. Μην ανοίγετε τους αγκώνες.\n7. Μικρή κλίση μπροστά.\n8. Συνεχής τάση από τροχαλία.\n9. Πολλές επαναλήψεις για πρήξιμο.\n10. Καθαρή απομόνωση τρικεφάλου.", "arm", "tricep_pushdown", "el", "τρικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Κάμψεις σε Μαξιλάρι (Preacher)", "1. Καθίστε στον πάγκο Scott.\n2. Μπράτσα πάνω στο μαξιλάρι.\n3. Λαβή στραβόμπαρας/αλτήρων.\n4. Πλήρες κατέβασμα για διάταση.\n5. Κάμψη μέχρι τους ώμους.\n6. Σφίξτε δυνατά στην κορυφή.\n7. Το μαξιλάρι κόβει την ορμή.\n8. Εστίαση στην κορυφή δικέφαλου.\n9. Για σχήμα και ύψος.\n10. Διατηρήστε αυστηρή φόρμα.", "arm", "preacher_curl", "el", "δικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Γαλλικές Πιέσεις (Skull Crushers)", "1. Ίσιος πάγκος με στραβόμπαρα.\n2. Χέρια πάνω από ώμους.\n3. Κατεβάστε προς το μέτωπο.\n4. Λυγίστε μόνο τους αγκώνες.\n5. Σταματήστε πριν το κεφάλι.\n6. Εκτείνετε στην αρχική θέση.\n7. Μπράτσα ακίνητα.\n8. Έντονη διάταση τρικεφάλου.\n9. Χρήση βοηθού για ασφάλεια.\n10. Γνωστό και ως γαλλικές πιέσεις.", "arm", "skull_crushers", "el", "τρικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Κάμψεις Συγκέντρωσης", "1. Καθιστοί, πόδια ανοιχτά.\n2. Αγκώνας στο εσωτερικό μηρού.\n3. Κάμψη αλτήρα προς στήθος.\n4. Εστίαση μόνο στον δικέφαλο.\n5. Καθόλου ορμή.\n6. Σφίξτε στην κορυφή.\n7. Αργή, ελεγχόμενη επιστροφή.\n8. Σύνδεση μυαλού-μυός.\n9. Μηδενική χρήση ώμου.\n10. Τελειώστε με πολλές επαναλήψεις.", "arm", "concentration_curl", "el", "δικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Εκτάσεις Τρικεφάλων πάνω από το Κεφάλι", "1. Όρθιοι ή καθιστοί.\n2. Κρατήστε αλτήρα με δύο χέρια.\n3. Σηκώστε πάνω από το κεφάλι.\n4. Κατεβάστε πίσω από τον αυχένα.\n5. Αγκώνες κοντά στα αυτιά.\n6. Εκτείνετε τα χέρια πάνω.\n7. Σφίξτε δυνατά τους τρικέφαλους.\n8. Ίσια σπονδυλική στήλη.\n9. Μην τοξοειδείτε τη μέση.\n10. Εστίαση στη μακρά κεφαλή.", "arm", "overhead_tricep_ext", "el", "τρικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Πιέσεις Πάγκου με Κλειστή Λαβή", "1. Ξαπλώστε στον πάγκο.\n2. Λαβή στο άνοιγμα των ώμων.\n3. Κατεβάστε στο κάτω στήθος.\n4. Αγκώνες κλειστοί μέσα.\n5. Σπρώξτε εκρηκτικά πάνω.\n6. Εστίαση στους τρικέφαλους.\n7. Μην ανοίγετε τους αγκώνες.\n8. Σταθεροί καρποί.\n9. Εισπνοή κάτω, εκπνοή πάνω.\n10. Βασική άσκηση όγκου τρικεφάλων.", "arm", "close_grip_bench", "el", "τρικέφαλοι,σύνθετη");
        addExerciseInternal(db, "Κάμψεις Zottman", "1. Όρθιοι με αλτήρες.\n2. Παλάμες πάνω, κάμψη.\n3. Στρέψτε παλάμες κάτω στην κορυφή.\n4. Κατεβάστε με παλάμες κάτω.\n5. Στρέψτε παλάμες πάλι πάνω.\n6. Συνδυάζει δικέφαλο/πήχη.\n7. Ελέγξτε την περιστροφή.\n8. Αργή κάθοδος.\n9. Κορμός ακίνητος.\n10. Βελτιώνει τη λαβή.", "arm", "zottman_curl", "el", "δικέφαλοι,πήχεις,απομόνωση");

        // --- ΠΟΔΙΑ (10) ---
        addExerciseInternal(db, "Καθίσματα (Squats)", "1. Πόδια στο άνοιγμα ώμων.\n2. Μπάρα στους τραπεζοειδείς.\n3. Στήθος ψηλά, πλάτη ίσια.\n4. Κάθισμα σε γοφούς και γόνατα.\n5. Μέχρι οι μηροί να είναι παράλληλοι.\n6. Σπρώξτε με φτέρνες για άνοδο.\n7. Γόνατα στην ίδια ευθεία με δάχτυλα.\n8. Εισπνοή κάτω, εκπνοή πάνω.\n9. Βασική άσκηση δύναμης.\n10. Τετρακέφαλοι, γλουτοί, κορμός.", "legs", "squats", "el", "τετρακέφαλοι,γλουτοί,σύνθετη");
        addExerciseInternal(db, "Πρέσα Ποδιών (Leg Press)", "1. Καθίστε γερά στο μηχάνημα.\n2. Πόδια στην πλατφόρμα.\n3. Απασφαλίστε τις λαβές.\n4. Κατεβάστε μέχρι τις 90 μοίρες.\n5. Μη σηκώνετε τη μέση.\n6. Σπρώξτε με τις φτέρνες.\n7. Μην κλειδώνετε τα γόνατα.\n8. Ελέγξτε το βάρος προσεκτικά.\n9. Αλλάξτε θέση ποδιών για εστίαση.\n10. Ασφαλής προπόνηση ποδιών.", "legs", "leg_press", "el", "τετρακέφαλοι,γλουτοί,σύνθετη");
        addExerciseInternal(db, "Προβολές (Lunges)", "1. Όρθιοι, πόδια μαζί.\n2. Μεγάλο βήμα μπροστά.\n3. Χαμηλώστε γοφούς στις 90 μοίρες.\n4. Πίσω γόνατο σχεδόν κάτω.\n5. Μπροστά γόνατο πάνω από αστράγαλο.\n6. Σπρώξτε πίσω στην αρχή.\n7. Αλλάξτε πόδια.\n8. Βελτιώνει ισορροπία.\n9. Γλουτοί και τετρακέφαλοι.\n10. Και με αλτήρες για βάρος.", "legs", "lunges", "el", "τετρακέφαλοι,γλουτοί,μονοποδική");
        addExerciseInternal(db, "Εκτάσεις Ποδιών (Leg Extension)", "1. Καθιστοί, πλάτη στο μαξιλάρι.\n2. Κνήμες κάτω από την μπάρα.\n3. Εκτείνετε μέχρι την ευθεία.\n4. Κρατήστε σύσπαση 1 δευτερόλεπτο.\n5. Κατεβάστε αργά.\n6. Μην κουνάτε το βάρος απότομα.\n7. Πιαστείτε από τις λαβές.\n8. Απομονώνει τους τετρακέφαλους.\n9. Ιδανικό για γράμμωση.\n10. Πολλές επαναλήψεις.", "legs", "leg_extension", "el", "τετρακέφαλοι,απομόνωση");
        addExerciseInternal(db, "Κάμψεις Ποδιών (Leg Curl)", "1. Μπρούμυτα στο μηχάνημα.\n2. Γόνατα στο σημείο περιστροφής.\n3. Λυγίστε προς τους γλουτούς.\n4. Σφίξτε δυνατά τους μηριαίους.\n5. Επιστρέψτε αργά.\n6. Γοφοί κολλημένοι κάτω.\n7. Εστίαση στην πίσω πλευρά.\n8. Δύναμη στο πίσω μέρος ποδιών.\n9. Βασικό για ισορροπία ποδιών.\n10. Προλαμβάνει τραυματισμούς γονάτων.", "legs", "leg_curl", "el", "μηριαίοι δικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Ακροστασίες (Calf Raises)", "1. Σταθείτε στην άκρη πλατφόρμας.\n2. Μόνο το μπροστινό μέρος πατάει.\n3. Χαμηλώστε φτέρνες για διάταση.\n4. Σπρώξτε γρήγορα στα δάχτυλα.\n5. Σφίξτε γάμπες στην κορυφή.\n6. Κρατήστε για λίγο.\n7. Κατεβάστε αργά.\n8. Όρθιοι ή καθιστοί.\n9. Πολλές επαναλήψεις.\n10. Στοχεύει τον γαστροκνήμιο.", "legs", "calf_raises", "el", "γάμπες,απομόνωση");
        addExerciseInternal(db, "Γέφυρα Γλουτών (Glute Bridge)", "1. Ανάσκελα, λυγισμένα γόνατα.\n2. Πόδια ίσια στο πάτωμα.\n3. Σηκώστε γοφούς προς το ταβάνι.\n4. Σφίξτε γλουτούς ψηλά.\n5. Κορμός σφιχτός.\n6. Κατεβάστε αργά.\n7. Μπάρα για έξτρα βάρος.\n8. Σταθερότητα ισχίων.\n9. Ενεργοποίηση γλουτών.\n10. Προστατεύει τη μέση.", "legs", "glute_bridge", "el", "γλουτοί,απομόνωση");
        addExerciseInternal(db, "Ρουμανικές Άρσεις Θανάτου (RDL)", "1. Πόδια στο άνοιγμα γοφών.\n2. Μπάρα μπροστά στους μηρούς.\n3. Λυγίστε από τους γοφούς.\n4. Κατεβάστε τη μπάρα κοντά στις κνήμες.\n5. Μέχρι τη διάταση των μηριαίων.\n6. Σπρώξτε τους γοφούς μπροστά.\n7. Σφίξτε γλουτούς πάνω.\n8. Γόνατα ελαφρώς λυγισμένα.\n9. Μην καμπουριάζετε τη μέση.\n10. Κορυφαία άσκηση μηριαίων.", "legs", "rdl", "el", "μηριαίοι,γλουτοί,σύνθετη");
        addExerciseInternal(db, "Ανεβάσματα σε Πάγκο (Step Ups)", "1. Μπροστά από πάγκο/κουτί.\n2. Ένα πόδι πάνω στην επιφάνεια.\n3. Σπρώξτε με τη φτέρνα.\n4. Ανεβείτε μέχρι την έκταση.\n5. Φέρτε και το άλλο πόδι πάνω.\n6. Κατεβείτε ελεγχόμενα.\n7. Αλλάξτε πόδι.\n8. Κορμός όρθιος.\n9. Ενεργός κορμός για ισορροπία.\n10. Μονομερής δύναμη ποδιών.", "legs", "step_ups", "el", "τετρακέφαλοι,γλουτοί,μονοποδική");
        addExerciseInternal(db, "Goblet Squat (Καθίσματα)", "1. Κρατήστε αλτήρα στο στήθος.\n2. Πόδια λίγο έξω από ώμους.\n3. Καθίστε ανάμεσα στα γόνατα.\n4. Στήθος ψηλά, πλάτη ίσια.\n5. Αγκώνες στο εσωτερικό των μηρών.\n6. Σπρώξτε πίσω στην αρχή.\n7. Πίεση σε όλο το πέλμα.\n8. Τάση στον κορμό.\n9. Εισπνοή κάτω, εκπνοή πάνω.\n10. Ιδανικό για βάθος καθίσματος.", "legs", "goblet_squat", "el", "τετρακέφαλοι,γλουτοί,σύνθετη");

        // --- ΩΜΟΙ (10) ---
        addExerciseInternal(db, "Πιέσεις Ώμων (Overhead Press)", "1. Πόδια στο άνοιγμα ώμων.\n2. Μπάρα στο ύψος στήθους.\n3. Πιέστε κάθετα πάνω.\n4. Κορμός σφιχτός, όχι τόξο.\n5. Κλειδώστε χέρια ψηλά.\n6. Επαναφορά στους ώμους.\n7. Κεφάλι έξω από τη διαδρομή.\n8. Πλήρης ανάπτυξη ώμων.\n9. Απαιτεί σταθερότητα.\n10. Στρατιωτική δύναμη.", "shoulders", "overhead_press", "el", "ώμοι,σύνθετη");
        addExerciseInternal(db, "Πλάγιες Εκτάσεις (Lateral Raise)", "1. Όρθιοι, αλτήρες στα πλάγια.\n2. Μικρή κάμψη αγκώνων.\n3. Σηκώστε στα πλάγια.\n4. Μέχρι το ύψος των ώμων.\n5. Κατεβάστε ελεγχόμενα.\n6. Καθόλου ορμή.\n7. Εστίαση στους πλάγιους δελτοειδείς.\n8. Δημιουργεί πλάτος ώμων.\n9. Μικρά δάχτυλα προς τα πάνω.\n10. Άσκηση απομόνωσης.", "shoulders", "lateral_raise", "el", "πλάγιοι δελτοειδείς,απομόνωση");
        addExerciseInternal(db, "Εμπρόσθιες Εκτάσεις (Front Raise)", "1. Κρατήστε αλτήρες μπροστά.\n2. Σηκώστε ένα χέρι τη φορά.\n3. Μέχρι το ύψος του ώμου.\n4. Χέρι σχεδόν ίσιο.\n5. Ελέγξτε το κατέβασμα.\n6. Πρόσθιοι δελτοειδείς.\n7. Εναλλάξ χέρια.\n8. Μην κουνάτε το σώμα.\n9. Ελαφριά προς μέτρια βάρη.\n10. Λεπτομέρεια πρόσθιου ώμου.", "shoulders", "front_raise", "el", "πρόσθιοι δελτοειδείς,απομόνωση");
        addExerciseInternal(db, "Face Pulls", "1. Σχοινί στο ύψος των ματιών.\n2. Τραβήξτε προς το πρόσωπο.\n3. Ανοίξτε τις άκρες του σχοινιού.\n4. Σφίξτε πίσω ώμους και τραπεζοειδείς.\n5. Εξωτερική περιστροφή στο τέλος.\n6. Αργή επιστροφή.\n7. Υγεία των ώμων.\n8. Διορθώνει την καμπουριαστή στάση.\n9. Πολλές επαναλήψεις.\n10. Πίσω μέρος ώμων.", "shoulders", "face_pulls", "el", "πίσω δελτοειδείς,τραπεζοειδείς");
        addExerciseInternal(db, "Άρσεις Τραπεζοειδών (Shrugs)", "1. Βαρείς αλτήρες ή μπάρα.\n2. Χέρια ίσια στα πλάγια.\n3. Σηκώστε ώμους προς τα αυτιά.\n4. Σφίξτε τραπεζοειδείς πάνω.\n5. Κρατήστε για ένα δευτερόλεπτο.\n6. Κατεβάστε αργά.\n7. Μην περιστρέφετε τους ώμους.\n8. Βαριά βάρη με ασφάλεια.\n9. Όγκος σε αυχένα και τραπέζι.\n10. Απλή αλλά αποτελεσματική.", "shoulders", "shrugs", "el", "τραπεζοειδείς,απομόνωση");
        addExerciseInternal(db, "Reverse Fly (Ανάποδα Ανοίγματα)", "1. Σκύψτε μπροστά από τη μέση.\n2. Πλάτη ίσια, κορμός σφιχτός.\n3. Σηκώστε βάρη στα πλάγια.\n4. Σφίξτε τις ωμοπλάτες.\n5. Πίσω δελτοειδείς.\n6. Αργή, ακριβής κίνηση.\n7. Κεφάλι σε ουδέτερη θέση.\n8. Όχι απότομες κινήσεις.\n9. Ισορροπία στην πλάτη.\n10. Βελτιώνει την πίσω όψη.", "shoulders", "reverse_fly", "el", "πίσω δελτοειδείς,απομόνωση");
        addExerciseInternal(db, "Arnold Press (Πιέσεις Άρνολντ)", "1. Καθιστοί, αλτήρες στο στήθος.\n2. Παλάμες προς το μέρος σας.\n3. Περιστρέψτε καθώς πιέζετε.\n4. Παλάμες προς τα έξω ψηλά.\n5. Αντίστροφη κίνηση κάτω.\n6. Πλήρες εύρος κίνησης.\n7. Από τον Arnold Schwarzenegger.\n8. Όλες οι κεφαλές του ώμου.\n9. Σφιχτός κορμός.\n10. Προχωρημένη άσκηση ώμων.", "shoulders", "arnold_press", "el", "ώμοι,σύνθετη");
        addExerciseInternal(db, "Όρθια Κωπηλατική (Upright Row)", "1. Όρθιοι με μπάρα/αλτήρες.\n2. Λαβή λίγο πιο κλειστή από ώμους.\n3. Τραβήξτε προς το πηγούνι.\n4. Οι αγκώνες οδηγούν την κίνηση.\n5. Βάρη κοντά στο σώμα.\n6. Παύση στην κορυφή.\n7. Κατεβάστε αργά.\n8. Μην κουνάτε το σώμα.\n9. Πλάγιοι δελτοειδείς/τραπέζια.\n10. Ελεγχόμενη κάθετη έλξη.", "shoulders", "upright_row", "el", "ώμοι,τραπεζοειδείς,σύνθετη");
        addExerciseInternal(db, "Στρατιωτικές Πιέσεις (Military Press)", "1. Όρθιοι με πόδια κλειστά.\n2. Μπάρα στο πάνω στήθος.\n3. Πιέστε την μπάρα κάθετα.\n4. Κεφάλι πίσω και μετά μπροστά.\n5. Κλειδώστε χέρια ψηλά.\n6. Χωρίς βοήθεια από πόδια.\n7. Σφίξτε δυνατά τους γλουτούς.\n8. Εκπνοή στην πίεση.\n9. Ελέγξτε την κάθοδο.\n10. Αυστηρή δύναμη ώμων.", "shoulders", "military_press", "el", "ώμοι,σύνθετη");
        addExerciseInternal(db, "Πλάγιες Εκτάσεις στην Τροχαλία", "1. Σταθείτε πλάγια στην τροχαλία.\n2. Λαβή στο μακρινό χέρι.\n3. Σηκώστε το χέρι στα πλάγια.\n4. Συνεχής τάση καλωδίου.\n5. Μέχρι το ύψος των ώμων.\n6. Επιστρέψτε αργά μπροστά.\n7. Σταθερός κορμός.\n8. Χωρίς ορμή.\n9. Απομονώνει τον πλάγιο δελτοειδή.\n10. Καλύτερη τάση από αλτήρες.", "shoulders", "cable_lateral_raise", "el", "ώμοι,απομόνωση");

        // --- ΠΛΑΤΗ (10) ---
        addExerciseInternal(db, "Έλξεις (Pull Ups)", "1. Λαβή πιο πλατιά από ώμους.\n2. Κρεμαστείτε με ίσια χέρια.\n3. Τραβήξτε στήθος προς την μπάρα.\n4. Σφίξτε φτερά στην κορυφή.\n5. Πηγούνι πάνω από την μπάρα.\n6. Κατεβείτε αργά.\n7. Μην κουνάτε τα πόδια.\n8. Ενεργός κορμός.\n9. Βασιλιάς των ασκήσεων πλάτης.\n10. Χτίζει πλάτος και δύναμη.", "back", "pull_ups", "el", "φτερά,σύνθετη");
        addExerciseInternal(db, "Άρσεις Θανάτου (Deadlift)", "1. Πόδια κάτω από την μπάρα.\n2. Ίσια πλάτη, πιάστε την μπάρα.\n3. Σπρώξτε με τα πόδια.\n4. Μπάρα κοντά στις κνήμες.\n5. Σταθείτε όρθιοι, κλειδώστε γοφούς.\n6. Ελεγχόμενη κάθοδος.\n7. Ουδέτερη σπονδυλική στήλη.\n8. Δύναμη σε όλο το σώμα.\n9. Βαριά βάρη.\n10. Πρώτα η τεχνική.", "back,legs", "deadlift", "el", "πλάτη,πόδια,σύνθετη");
        addExerciseInternal(db, "Κωπηλατική με Μπάρα (Bent Over Row)", "1. Σκύψτε, πλάτη ίσια.\n2. Πιάστε την μπάρα πρηνή λαβή.\n3. Τραβήξτε προς το κάτω μέρος κοιλιάς.\n4. Σφίξτε δυνατά την πλάτη.\n5. Κατεβάστε ελεγχόμενα.\n6. Κορμός πολύ σφιχτός.\n7. Χτίζει το πάχος της πλάτης.\n8. Χωρίς ορμή.\n9. Προστατέψτε τη μέση.\n10. Αποτελεσματική κωπηλατική.", "back", "bent_over_row", "el", "πλάτη,σύνθετη");
        addExerciseInternal(db, "Έλξεις στην Τροχαλία (Lat Pulldown)", "1. Καθίστε στο μηχάνημα.\n2. Πλατιά λαβή, παλάμες έξω.\n3. Τραβήξτε στο πάνω στήθος.\n4. Μικρή κλίση πίσω.\n5. Σφίξτε φτερά κάτω.\n6. Αργή απελευθέρωση πάνω.\n7. Ελέγξτε τη διάταση.\n8. Εναλλακτική των έλξεων.\n9. Πλάτος πάνω πλάτης.\n10. Ρυθμιζόμενη αντίσταση.", "back", "lat_pulldown", "el", "φτερά,απομόνωση");
        addExerciseInternal(db, "Καθιστή Κωπηλατική (Cable Row)", "1. Καθίστε, πόδια στα στηρίγματα.\n2. Μικρή κάμψη γονάτων.\n3. Τραβήξτε λαβή στον ομφαλό.\n4. Ώμοι πίσω.\n5. Σφίξτε ωμοπλάτες μαζί.\n6. Τεντώστε χέρια για διάταση.\n7. Κορμός σταθερός.\n8. Πάχος μέσης πλάτης.\n9. Συνεχής τάση τροχαλίας.\n10. Διάφορες επιλογές λαβής.", "back", "seated_cable_row", "el", "πλάτη,σύνθετη");
        addExerciseInternal(db, "Κωπηλατική T-Bar", "1. Πάνω από την μπάρα T-bar.\n2. Λαβή V.\n3. Τραβήξτε προς το στήθος.\n4. Σφίξτε μέση πλάτη-τραπέζια.\n5. Κατεβάστε στο πάτωμα.\n6. Διατηρήστε ίσια πλάτη.\n7. Παραδοσιακή άσκηση όγκου.\n8. Έντονη απομόνωση πλάτης.\n9. Σφίξτε γερά τον κορμό.\n10. Δύναμη στην πλάτη.", "back", "t_bar_row", "el", "πλάτη,σύνθετη");
        addExerciseInternal(db, "Κωπηλατική με Αλτήρα (Dumbbell Row)", "1. Ένα χέρι και γόνατο στον πάγκο.\n2. Ίσια πλάτη, άλλο χέρι κάτω.\n3. Τραβήξτε αλτήρα στον γοφό.\n4. Σφίξτε φτερά και τραπεζοειδείς.\n5. Κατεβάστε με πλήρη διάταση.\n6. Μία πλευρά τη φορά.\n7. Βελτιώνει τη μυϊκή ισορροπία.\n8. Μεγάλο εύρος κίνησης.\n9. Μη στρίβετε τον κορμό.\n10. Αποτελεσματική απομόνωση.", "back", "dumbbell_row", "el", "πλάτη,μονομερής");
        addExerciseInternal(db, "Ραχιαίοι (Hyperextensions)", "1. Τοποθετηθείτε στον πάγκο.\n2. Αστράγαλοι στα στηρίγματα.\n3. Σκύψτε από τους γοφούς.\n4. Κορμός προς το πάτωμα.\n5. Ανεβείτε μέχρι την ευθεία.\n6. Σφίξτε μέση και γλουτούς.\n7. Μην υπερεκτείνετε πάνω.\n8. Χέρια πίσω από κεφάλι.\n9. Αργή, ρυθμική κίνηση.\n10. Βασικό για τη μέση.", "back", "hyperextensions", "el", "πλάτη,σύνθετη");
        addExerciseInternal(db, "Έλξεις Τροχαλίας με Κλειστή Λαβή", "1. Λαβή V στην τροχαλία.\n2. Καθίστε και ασφαλίστε πόδια.\n3. Μικρή κλίση πίσω.\n4. Τραβήξτε τη λαβή στο στήθος.\n5. Σφίξτε φτερά και πλάτη.\n6. Τεντώστε τα χέρια ψηλά.\n7. Ελέγξτε το βάρος.\n8. Μην χρησιμοποιείτε το βάρος σώματος.\n9. Για τα κάτω φτερά.\n10. Βελτιώνει το πάχος πλάτης.", "back", "close_grip_pulldown", "el", "πλάτη,φτερά,σύνθετη");
        addExerciseInternal(db, "Κωπηλατική με ένα Χέρι στην Τροχαλία", "1. Σταθείτε στην τροχαλία.\n2. Ένα γόνατο στο έδαφος.\n3. Τραβήξτε τη λαβή στον γοφό.\n4. Στρέψτε ελαφρά την παλάμη.\n5. Νιώστε τη σύσπαση στο φτερό.\n6. Πλήρης διάταση στην αρχή.\n7. Ώμοι στην ίδια ευθεία.\n8. Κάθε πλευρά χωριστά.\n9. Εξαλείφει ανισορροπίες.\n10. Εστιασμένη απομόνωση πλάτης.", "back", "single_arm_row", "el", "πλάτη,μονομερής");

        // --- ΚΟΙΛΙΑΚΟΙ (10) ---
        addExerciseInternal(db, "Ροκανίσματα", "1. Ανάσκελα, λυγισμένα γόνατα.\n2. Χέρια ελαφρά πίσω από κεφάλι.\n3. Σηκώστε ώμους από πάτωμα.\n4. Σφίξτε κοιλιακούς πάνω.\n5. Εκπνοή στην άνοδο.\n6. Μέση κολλημένη κάτω.\n7. Ποιότητα αντί για ποσότητα.\n8. Μην τραβάτε τον αυχένα.\n9. Πάνω κοιλιακοί.\n10. Κλασική κίνηση κορμού.", "abs", "crunches", "el", "κοιλιακοί,απομόνωση");
        addExerciseInternal(db, "Σανίδα", "1. Στους πήχεις και τα δάχτυλα.\n2. Σώμα σε ευθεία γραμμή.\n3. Σφίξτε γλουτούς και κοιλιά.\n4. Μη χαμηλώνετε τους γοφούς.\n5. Ουδέτερος αυχένας.\n6. Αναπνέετε σταθερά.\n7. Κρατήστε για μέγιστο χρόνο.\n8. Αντοχή κορμού.\n9. Ισομετρική σταθερότητα.\n10. Χωρίς εξοπλισμό.", "abs", "plank", "el", "κορμός,σταθερότητα");
        addExerciseInternal(db, "Άρσεις Ποδιών", "1. Ανάσκελα, χέρια στα πλάγια.\n2. Πόδια τεντωμένα.\n3. Σηκώστε στις 90 μοίρες.\n4. Κατεβάστε αργά, χωρίς επαφή.\n5. Ελέγξτε την κίνηση.\n6. Πιέστε τη μέση κάτω.\n7. Κάτω κοιλιακοί.\n8. Και κρεμαστή εκδοχή.\n9. Συνεχής τάση.\n10. Αποτελεσματικό για κορμό.", "abs", "leg_raises", "el", "κοιλιακοί,απομόνωση");
        addExerciseInternal(db, "Russian Twist", "1. Καθιστοί, λυγισμένα γόνατα.\n2. Μικρή κλίση πίσω.\n3. Περιστρέψτε κορμό δεξιά-αριστερά.\n4. Ακουμπήστε πάτωμα με χέρια.\n5. Πόδια ψηλά για δυσκολία.\n6. Εστίαση στους πλάγιους.\n7. Ελέγξτε την περιστροφή.\n8. Χρήση βάρους για ένταση.\n9. Περιστροφική δύναμη.\n10. Πλάγιοι κοιλιακοί.", "abs", "russian_twist", "el", "πλάγιοι,απομόνωση");
        addExerciseInternal(db, "Bicycle Crunches", "1. Ανάσκελα, πόδια ψηλά.\n2. Αγκώνας σε αντίθετο γόνατο.\n3. Εναλλάξ κίνηση ποδηλάτου.\n4. Πλήρης περιστροφή κορμού.\n5. Σφίξτε δυνατά την κοιλιά.\n6. Ελεγχόμενη ταχύτητα.\n7. Όλος ο κορμός.\n8. Πλάγιοι κοιλιακοί.\n9. Καίει περισσότερες θερμίδες.\n10. Δυναμική παραλλαγή.", "abs", "bicycle_crunches", "el", "κοιλιακοί,πλάγιοι");
        addExerciseInternal(db, "Mountain Climbers", "1. Θέση υψηλής σανίδας.\n2. Γόνατα στο στήθος γρήγορα.\n3. Εναλλάξ πόδια ταχύτατα.\n4. Γοφοί χαμηλά.\n5. Σφιχτός κορμός.\n6. Καρδιοαναπνευστική και κοιλιά.\n7. Γρήγορη κίνηση.\n8. Λειτουργική ταχύτητα.\n9. Κάψτε λίπος χτίζοντας κοιλιά.\n10. Υψηλή ένταση.", "abs", "mountain_climbers", "el", "κορμός,καρδιοαναπνευστική");
        addExerciseInternal(db, "Άρσεις Γονάτων", "1. Κρεμαστείτε από μπάρα έλξεων.\n2. Φέρτε γόνατα στο στήθος.\n3. Σφίξτε κοιλιακούς πάνω.\n4. Κατεβάστε αργά.\n5. Μην κουνάτε το σώμα.\n6. Προχωρημένη άσκηση.\n7. Κυρίως κάτω κοιλιακοί.\n8. Απαιτεί δύναμη λαβής.\n9. Αυστηρή φόρμα.\n10. Δυνατός κορμός.", "abs", "hanging_knee_raise", "el", "κοιλιακοί,απομόνωση");
        addExerciseInternal(db, "Ψαλιδάκια (Flutter Kicks)", "1. Ανάσκελα στο πάτωμα.\n2. Χέρια κάτω από γλουτούς.\n3. Πόδια 15 εκ. από το έδαφος.\n4. Εναλλάξ ψαλιδάκια πάνω/κάτω.\n5. Πόδια τεντωμένα.\n6. Μην ακουμπάτε κάτω.\n7. Σφίξτε κάτω κοιλιακούς.\n8. Μικρές, γρήγορες κινήσεις.\n9. Αναπνέετε φυσιολογικά.\n10. Έντονη άσκηση για κοιλιά.", "abs", "flutter_kicks", "el", "κοιλιακοί,απομόνωση");
        addExerciseInternal(db, "Dead Bug", "1. Ανάσκελα, άκρα ψηλά.\n2. Γόνατα στις 90 μοίρες.\n3. Κατεβάστε αντίθετο χέρι/πόδι.\n4. Μέση κολλημένη στο έδαφος.\n5. Επιστροφή στην αρχή.\n6. Αλλάξτε πλευρά.\n7. Πολύ αργή κίνηση.\n8. Εστίαση στη σταθερότητα.\n9. Μην τοξοειδείτε τη μέση.\n10. Ασφαλές για τη σπονδυλική.", "abs", "dead_bug", "el", "κοιλιακοί,σταθερότητα");
        addExerciseInternal(db, "Σανίδα με Κλίση Γοφών", "1. Θέση σανίδας στους πήχεις.\n2. Σώμα σε ευθεία γραμμή.\n3. Στρέψτε γοφούς στη μία πλευρά.\n4. Ακουμπήστε γοφό στο έδαφος.\n5. Στρέψτε στην άλλη πλευρά.\n6. Σφιχτός κορμός.\n7. Ελεγχόμενη κίνηση τόξου.\n8. Μην βουλιάζετε τη μέση.\n9. Ενεργοποιεί πλάγιους/κορμό.\n10. Δυναμική παραλλαγή σανίδας.", "abs", "plank_hip_dips", "el", "κοιλιακοί,πλάγιοι");
    }

    private void addExerciseInternal(SQLiteDatabase db, String title, String description,
                                     String muscleGroup, String imageName, String lang, String tags) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_MUSCLE_GROUP, muscleGroup);
        values.put(COLUMN_IMAGE_NAME, imageName);
        values.put(COLUMN_LANGUAGE, lang);
        values.put(COLUMN_TAGS, tags);
        db.insert(TABLE_EXERCISES, null, values);
    }
}
