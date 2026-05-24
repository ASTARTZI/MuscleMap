package com.example.muscleapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 24;
    private static final String DATABASE_NAME = "exerciseDB.db";
    
    public static final String TABLE_EXERCISES = "exercises";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_MUSCLE_GROUP = "muscle_group";
    public static final String COLUMN_IMAGE_NAME = "image_name";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_TAGS = "tags";

    // New Table for Local User Programs
    public static final String TABLE_PROGRAMS = "user_programs";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PROGRAM_DATA = "program_json";

    public ExerciseDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_MUSCLE_GROUP + " TEXT,"
                + COLUMN_IMAGE_NAME + " TEXT,"
                + COLUMN_LANGUAGE + " TEXT,"
                + COLUMN_TAGS + " TEXT" + ")";
        db.execSQL(CREATE_EXERCISES_TABLE);

        String CREATE_PROGRAMS_TABLE = "CREATE TABLE " + TABLE_PROGRAMS + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                + COLUMN_PROGRAM_DATA + " TEXT" + ")";
        db.execSQL(CREATE_PROGRAMS_TABLE);

        insertDefaultExercises(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 24) {
            String CREATE_PROGRAMS_TABLE = "CREATE TABLE " + TABLE_PROGRAMS + "("
                    + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                    + COLUMN_PROGRAM_DATA + " TEXT" + ")";
            db.execSQL(CREATE_PROGRAMS_TABLE);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Just recreate if moving backwards during development
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAMS);
        onCreate(db);
    }

    // --- Local Program Management ---

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
        if (cursor.moveToFirst()) {
            json = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return json;
    }

    public void deleteLocalProgram(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROGRAMS, COLUMN_USER_ID + " = ?", new String[]{userId});
        db.close();
    }

    // --- Exercise Management ---

    public List<Exercise> getExercisesByMuscleGroup(String muscleGroup, String lang) {
        List<Exercise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "(" + COLUMN_MUSCLE_GROUP + " = ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ? OR " +
                COLUMN_MUSCLE_GROUP + " LIKE ?) AND " +
                COLUMN_LANGUAGE + " = ?";

        String[] selectionArgs = new String[]{
                muscleGroup,
                muscleGroup + ",%",
                "%," + muscleGroup,
                "%," + muscleGroup + ",%",
                lang
        };

        Cursor cursor = db.query(TABLE_EXERCISES, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(6)
                );
                list.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Exercise getExerciseByImageName(String imageName, String lang) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES, null,
                COLUMN_IMAGE_NAME + " = ? AND " + COLUMN_LANGUAGE + " = ?",
                new String[]{imageName, lang},
                null, null, null);

        Exercise exercise = null;
        if (cursor.moveToFirst()) {
            exercise = new Exercise(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(6)
            );
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
        addExerciseInternal(db, "Incline Bench Press", "The Incline Bench Press targets the upper chest.\nAdjust the bench to 30-45 degrees.\nKeep your feet flat on the floor.\nLower the bar slowly to the upper chest.\nAvoid bouncing the bar.\nKeep your wrists straight.\nBreathe correctly.\nDo not use too much weight.\nKeep shoulder blades retracted.", "chest", "incline_bench_press", "en", "upper chest,compound");
        addExerciseInternal(db, "Flat Bench Press", "The Flat Bench Press builds overall chest strength.\nTargets pectorals, triceps and front shoulders.\nLie flat on the bench.\nLower the bar to the middle of your chest.\nAvoid flaring your elbows.\nPush the bar upward smoothly.\nMaintain a natural arch in your back.\nUse a spotter for heavy weights.\nFocus on correct technique.", "chest", "flat_bench_press", "en", "middle chest,compound");
        addExerciseInternal(db, "Chest Fly", "The Chest Fly is an isolation exercise for the chest.\nKeep a slight bend in your elbows.\nOpen your arms until you feel a stretch.\nAvoid lowering weights too far.\nBring weights together by squeezing the chest.\nDo not turn it into a press.\nUse lighter weights.\nKeep your posture stable.\nBreathe properly.", "chest", "chest_fly", "en", "inner chest,isolation");
        addExerciseInternal(db, "Bicep Curl", "The Bicep Curl strengthens the biceps.\nStand upright with elbows close to torso.\nCurl the weight while keeping upper arms still.\nAvoid swinging your body.\nSqueeze biceps at the top.\nLower the weight slowly.\nKeep wrists neutral.\nUse a full range of motion.\nControlled reps are key.", "arm", "bicep_curl", "en", "biceps,isolation");
        addExerciseInternal(db, "Tricep Dips", "Tricep Dips target the triceps.\nCan be done on parallel bars or a bench.\nKeep chest lifted and elbows back.\nLower your body until elbows reach 90 degrees.\nAvoid going too low.\nPush yourself up using your triceps.\nStay upright for tricep focus.\nBeginners can use assistance.\nMove slowly.", "arm,chest", "tricep_dips", "en", "triceps,lower chest,compound");
        addExerciseInternal(db, "Squats", "Squats target quads, glutes, and hamstrings.\nStand with feet shoulder-width apart.\nLower your hips back and down.\nKeep knees aligned with toes.\nAvoid letting knees collapse.\nKeep spine neutral and core tight.\nPush through heels to stand up.\nDo not lean too far forward.\nWarm up properly.", "legs", "squats", "en", "quads,glutes,compound");
        addExerciseInternal(db, "Leg Press", "Leg Press strengthens legs safely.\nTargets quads, glutes, and hamstrings.\nSit firmly with feet on the platform.\nLower platform slowly to 90 degrees.\nAvoid bringing knees too close to chest.\nPush platform up through your heels.\nKeep lower back pressed against seat.\nUse controlled motion.\nIncrease weight gradually.", "legs", "leg_press", "en", "quads,glutes,compound");
        addExerciseInternal(db, "Overhead Press", "Overhead Press builds shoulder strength.\nTargets deltoids, triceps and core.\nStand with feet shoulder-width apart.\nPress weight overhead in a straight path.\nAvoid excessive back arching.\nLower weight slowly to shoulders.\nKeep core tight.\nStart with manageable weight.\nBreathe steadily.", "shoulders", "overhead_press", "en", "front delts,compound");
        addExerciseInternal(db, "Lateral Raise", "Lateral Raise targets the side deltoids.\nHelps develop shoulder width.\nHold dumbbells at your sides.\nRaise arms outward to shoulder height.\nAvoid lifting too high.\nDo not swing your body.\nLower weights slowly.\nUse lighter weights for form.\nKeep shoulders relaxed.", "shoulders", "lateral_raise", "en", "side delts,isolation");
        addExerciseInternal(db, "Pull Ups", "Pull Ups strengthen the back and arms.\nTarget lats, biceps and upper back.\nGrip bar wider than shoulders.\nPull body up until chin passes bar.\nAvoid swinging.\nLower yourself slowly.\nKeep core engaged.\nBeginners can use bands.\nFocus on shoulder position.", "back,arm", "pull_ups", "en", "lats,biceps,compound");
        addExerciseInternal(db, "Deadlift", "Deadlift builds total body strength.\nTargets back, glutes, and hamstrings.\nStand with feet hip-width apart.\nMaintain a neutral spine.\nLift weight by driving through legs.\nAvoid rounding your back.\nStand tall at the top.\nLower the bar with control.\nPrioritize technique.", "back,legs", "deadlift", "en", "lower back,hamstrings,compound");
        addExerciseInternal(db, "Crunches", "Crunches strengthen the core.\nLie on your back with knees bent.\nPlace hands behind head lightly.\nLift shoulders slightly off floor.\nAvoid pulling your neck.\nMove slowly and avoid momentum.\nExhale as you crunch up.\nKeep lower back stable.\nQuality over speed.", "abs", "crunches", "en", "upper abs,isolation");
        addExerciseInternal(db, "Plank", "Plank is an isometric core exercise.\nEngages abs, shoulders, and glutes.\nPlace forearms on floor, body straight.\nEngage abs and glutes.\nAvoid letting hips sag or rise.\nKeep neck neutral.\nBreathe steadily.\nStart with shorter holds.\nForm is most important.", "abs", "plank", "en", "core,isometric");
    }

    private void insertGreekExercises(SQLiteDatabase db) {
        addExerciseInternal(db, "Πιέσεις Πάγκου με Κλίση", "Οι πιέσεις πάγκου με κλίση στοχεύουν το πάνω μέρος του στήθους.\nΡυθμίστε τον πάγκο στις 30-45 μοίρες.\nΚρατήστε τα πόδια στο πάτωμα.\nΚατεβάστε τη μπάρα αργά στο στήθος.\nΜην αναπηδάτε τη μπάρα.\nΚρατήστε τους καρπούς ίσιους.\nΑναπνέετε σωστά.\nΜην βάζετε υπερβολικό βάρος.\nΚρατήστε τις ωμοπλάτες πίσω.", "chest", "incline_bench_press", "el", "πάνω στήθος,σύνθετη");
        addExerciseInternal(db, "Πιέσεις σε Ίσιο Πάγκο", "Οι πιέσεις σε ίσιο πάγκο χτίζουν δύναμη στο στήθος.\nΣτοχεύουν θωρακικούς, τρικέφαλους και ώμους.\nΞαπλώστε στον πάγκο.\nΚατεβάστε τη μπάρα στο κέντρο του στήθους.\nΜην ανοίγετε πολύ τους αγκώνες.\nΣπρώξτε τη μπάρα πάνω ομαλά.\nΔιατηρήστε φυσική καμπύλη στη μέση.\nΧρησιμοποιήστε βοηθό στα πολλά βάρη.\nΕστιάστε στη σωστή τεχνική.", "chest", "flat_bench_press", "el", "μεσαίο στήθος,σύνθετη");
        addExerciseInternal(db, "Ανοίγματα Στήθους", "Τα ανοίγματα στήθους είναι άσκηση απομόνωσης.\nΚρατήστε ελαφριά κάμψη στους αγκώνες.\nΑνοίξτε τα χέρια μέχρι να νιώσετε διάταση.\nΜην κατεβάζετε τα βάρη πολύ χαμηλά.\nΦέρτε τα βάρη μαζί σφίγγοντας το στήθος.\nΜην την μετατρέπετε σε πιέσεις.\nΧρησιμοποιήστε ελαφριά βάρη.\nΚρατήστε τη στάση σας σταθερή.\nΑναπνέετε σωστά.", "chest", "chest_fly", "el", "εσωτερικό στήθος,απομόνωση");
        addExerciseInternal(db, "Κάμψεις Δικεφάλων", "Οι κάμψεις δικεφάλων δυναμώνουν τους δικέφαλους.\nΣταθείτε όρθιοι με αγκώνες κοντά στον κορμό.\nΛυγίστε το βάρος με τα μπράτσα ακίνητα.\nΜην κουνάτε το σώμα σας.\nΣφίξτε τους δικέφαλους στην κορυφή.\nΚατεβάστε το βάρος αργά.\nΚρατήστε τους καρπούς σταθερούς.\nΚάντε πλήρη κίνηση.\nΟι ελεγχόμενες επαναλήψεις μετρούν.", "arm", "bicep_curl", "el", "δικέφαλοι,απομόνωση");
        addExerciseInternal(db, "Βυθίσεις Τρικεφάλων", "Οι βυθίσεις στοχεύουν τους τρικέφαλους.\nΓίνονται σε δίζυγο ή σε πάγκο.\nΣτήθος ψηλά και αγκώνες προς τα πίσω.\nΚατεβείτε μέχρι τις 90 μοίρες.\nΜην πάτε πολύ χαμηλά.\nΣπρώξτε πάνω με τους τρικέφαλους.\nΜείνετε όρθιοι για τους τρικέφαλους.\nΟι αρχάριοι χρησιμοποιούν υποβοήθηση.\nΚινηθείτε αργά.", "arm,chest", "tricep_dips", "el", "τρικέφαλοι,κάτω στήθος,σύνθετη");
        addExerciseInternal(db, "Καθίσματα", "Τα καθίσματα γυμνάζουν πόδια και γλουτούς.\nΠόδια στο άνοιγμα των ώμων.\nΚατεβάστε τους γοφούς πίσω και κάτω.\nΓόνατα στην ίδια ευθεία με τα δάχτυλα.\nΜην αφήνετε τα γόνατα να κλείνουν.\nΣπονδυλική στήλη ίσια και κορμός σφιχτός.\nΣπρώξτε με τις φτέρνες για να σηκωθείτε.\nΜην γέρνετε πολύ μπροστά.\nΚάντε ζέσταμα.", "legs", "squats", "el", "τετρακέφαλοι,γλουτοί,σύνθετη");
        addExerciseInternal(db, "Πρέσα Ποδιών", "Η πρέσα δυναμώνει τα πόδια με ασφάλεια.\nΣτοχεύει τετρακέφαλους και γλουτούς.\nΚαθίστε σταθερά με τα πόδια στην πλατφόρμα.\nΚατεβάστε αργά μέχρι τις 90 μοίρες.\nΜην φέρνετε γόνατα πολύ κοντά στο στήθος.\nΣπρώξτε πάνω με τις φτέρνες.\nΜέση κολλημένη στο κάθισμα.\nΧρησιμοποιήστε ελεγχόμενη κίνηση.\nΑυξήστε το βάρος σταδιακά.", "legs", "leg_press", "el", "τετρακέφαλοι,γλουτοί,σύνθετη");
        addExerciseInternal(db, "Πιέσεις Ώμων", "Οι πιέσεις ώμων χτίζουν δύναμη στο πάνω σώμα.\nΣτοχεύουν δελτοειδείς και τρικέφαλους.\nΠόδια στο άνοιγμα των ώμων.\nΠιέστε πάνω σε ευθεία γραμμή.\nΜην τοξοειδείτε τη μέση.\nΚατεβάστε αργά στο ύψος των ώμων.\nΚρατήστε τον κορμό σφιχτό.\nΞεκινήστε με λίγα βάρη.\nΑναπνέετε σταθερά.", "shoulders", "overhead_press", "el", "μπροστινοί δελτοειδείς,σύνθετη");
        addExerciseInternal(db, "Πλάγιες Εκτάσεις", "Οι πλάγιες εκτάσεις στοχεύουν τους ώμους.\nΒοηθούν στο πλάτος των ώμων.\nΚρατήστε τους αλτήρες στα πλάγια.\nΣηκώστε μέχρι το ύψος των ώμων.\nΜην σηκώνετε πολύ γρήγορα.\nΜην κουνάτε το σώμα σας.\nΚατεβάστε τα βάρη αργά.\nΧρησιμοποιήστε ελαφριά βάρη.\nΚρατήστε τους ώμους χαλαρούς.", "shoulders", "lateral_raise", "el", "πλάγιοι δελτοειδείς,απομόνωση");
        addExerciseInternal(db, "Έλξεις στο Μονόζυγο", "Οι έλξεις δυναμώνουν την πλάτη.\nΣτοχεύουν φτερά και δικέφαλους.\nΠιάστε τη μπάρα έξω από τους ώμους.\nΤραβήξτε μέχρι το πηγούνι να περάσει.\nΜην κουνιέστε για ορμή.\nΚατεβείτε αργά.\nΚρατήστε τον κορμό ενεργό.\nΟι αρχάριοι χρησιμοποιούν λάστιχα.\nΕστιάστε στους ώμους.", "back,arm", "pull_ups", "el", "φτερά,δικέφαλοι,σύνθετη");
        addExerciseInternal(db, "Άρσεις Θανάτου", "Οι άρσεις θανάτου χτίζουν όλο το σώμα.\nΣτοχεύουν πλάτη, γλουτούς και μέση.\nΠόδια στο άνοιγμα των γοφών.\nΊσια πλάτη σε όλη τη διάρκεια.\nΣηκώστε σπρώχνοντας με τα πόδια.\nΜην καμπουριάζετε την πλάτη.\nΣταθείτε όρθιοι στην κορυφή.\nΚατεβάστε ελεγχόμενα.\nΤεχνική πάνω από όλα.", "back,legs", "deadlift", "el", "μέση,μηριαίοι δικέφαλοι,σύνθετη");
        addExerciseInternal(db, "Ροκανίσματα", "Τα ροκανίσματα δυναμώνουν τους κοιλιακούς.\nΑνάσκελα με λυγισμένα γόνατα.\nΧέρια πίσω από το κεφάλι ελαφρά.\nΣηκώστε ώμους λίγο από το πάτωμα.\nΜην τραβάτε τον αυχένα.\nΚινηθείτε αργά.\nΕκπνεύστε καθώς ανεβαίνετε.\nΚρατήστε τη μέση σταθερή.\nΠοιότητα αντί για ταχύτητα.", "abs", "crunches", "el", "πάνω κοιλιακοί,απομόνωση");
        addExerciseInternal(db, "Σανίδα", "Η σανίδα γυμνάζει όλο τον κορμό.\nΠήχεις στο πάτωμα, σώμα ίσιο.\nΣφίξτε κοιλιακούς και γλουτούς.\nΜην αφήνετε τη μέση να πέφτει.\nΑυχένας σε ουδέτερη θέση.\nΑναπνέετε σταθερά.\nΞεκινήστε με λίγα δευτερόλεπτα.\nΗ σωστή στάση είναι το παν.", "abs", "plank", "el", "κορμός,ισομετρική");
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
