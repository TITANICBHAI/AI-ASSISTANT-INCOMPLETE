package com.aiassistant.ai.features.education.jee.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages persistence of learning data across app sessions
 * This ensures that the AI Assistant remembers what it learned
 * even after the app is closed and reopened.
 */
public class LearningPersistenceManager {
    private static final String TAG = "LearningPersistenceMgr";
    
    // Preference constants
    private static final String PREFS_NAME = "learning_persistence_prefs";
    private static final String KEY_LAST_SAVE_TIME = "last_save_time";
    private static final String KEY_LEARNING_COUNT = "learning_count";
    
    // File names
    private static final String PHYSICS_KNOWLEDGE_FILE = "physics_knowledge.data";
    private static final String MATH_KNOWLEDGE_FILE = "math_knowledge.data";
    private static final String CHEMISTRY_KNOWLEDGE_FILE = "chemistry_knowledge.data";
    private static final String EMOTIONAL_STATE_FILE = "emotional_state.data";
    private static final String LEARNING_HISTORY_FILE = "learning_history.data";
    private static final String PREFERENCES_FILE = "learning_preferences.data";
    
    private Context context;
    private SharedPreferences preferences;
    
    /**
     * Constructor
     * @param context Android context
     */
    public LearningPersistenceManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        Log.i(TAG, "LearningPersistenceManager initialized");
    }
    
    /**
     * Save learning data to persistent storage
     * @param learningData The data to save
     * @return Success status
     */
    public boolean saveLearningData(LearningData learningData) {
        Log.i(TAG, "Saving learning data");
        
        boolean success = true;
        
        // Save subject-specific knowledge
        success &= saveObjectToFile(learningData.getPhysicsKnowledge(), PHYSICS_KNOWLEDGE_FILE);
        success &= saveObjectToFile(learningData.getMathKnowledge(), MATH_KNOWLEDGE_FILE);
        success &= saveObjectToFile(learningData.getChemistryKnowledge(), CHEMISTRY_KNOWLEDGE_FILE);
        
        // Save emotional state and preferences
        success &= saveObjectToFile(learningData.getEmotionalState(), EMOTIONAL_STATE_FILE);
        success &= saveObjectToFile(learningData.getLearningHistory(), LEARNING_HISTORY_FILE);
        success &= saveObjectToFile(learningData.getPreferences(), PREFERENCES_FILE);
        
        // Update preferences
        if (success) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis());
            editor.putInt(KEY_LEARNING_COUNT, learningData.getLearningCount());
            editor.apply();
        }
        
        Log.i(TAG, "Learning data saved: " + success);
        return success;
    }
    
    /**
     * Load learning data from persistent storage
     * @return The loaded data or null if not found
     */
    public LearningData loadLearningData() {
        Log.i(TAG, "Loading learning data");
        
        // Check if there's saved data
        if (!preferences.contains(KEY_LAST_SAVE_TIME)) {
            Log.i(TAG, "No saved learning data found");
            return null;
        }
        
        try {
            // Create a new learning data object
            LearningData learningData = new LearningData();
            
            // Load subject-specific knowledge
            Map<String, Object> physicsKnowledge = loadObjectFromFile(PHYSICS_KNOWLEDGE_FILE);
            Map<String, Object> mathKnowledge = loadObjectFromFile(MATH_KNOWLEDGE_FILE);
            Map<String, Object> chemistryKnowledge = loadObjectFromFile(CHEMISTRY_KNOWLEDGE_FILE);
            
            if (physicsKnowledge != null) {
                learningData.setPhysicsKnowledge(physicsKnowledge);
            }
            
            if (mathKnowledge != null) {
                learningData.setMathKnowledge(mathKnowledge);
            }
            
            if (chemistryKnowledge != null) {
                learningData.setChemistryKnowledge(chemistryKnowledge);
            }
            
            // Load emotional state and preferences
            Object emotionalState = loadObjectFromFile(EMOTIONAL_STATE_FILE);
            Object learningHistory = loadObjectFromFile(LEARNING_HISTORY_FILE);
            Map<String, Float> preferences = loadObjectFromFile(PREFERENCES_FILE);
            
            if (emotionalState != null) {
                learningData.setEmotionalState(emotionalState);
            }
            
            if (learningHistory != null) {
                learningData.setLearningHistory(learningHistory);
            }
            
            if (preferences != null) {
                learningData.setPreferences(preferences);
            }
            
            // Set learning count
            learningData.setLearningCount(preferences.getInt(KEY_LEARNING_COUNT, 0));
            
            Log.i(TAG, "Learning data loaded successfully");
            return learningData;
        } catch (Exception e) {
            Log.e(TAG, "Error loading learning data", e);
            return null;
        }
    }
    
    /**
     * Get the last time learning data was saved
     * @return Timestamp in milliseconds, or 0 if never saved
     */
    public long getLastSaveTime() {
        return preferences.getLong(KEY_LAST_SAVE_TIME, 0);
    }
    
    /**
     * Save an object to a file
     * @param object Object to save
     * @param fileName File name
     * @return Success status
     */
    private boolean saveObjectToFile(Object object, String fileName) {
        if (object == null) {
            return false;
        }
        
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving object to file: " + fileName, e);
            return false;
        }
    }
    
    /**
     * Load an object from a file
     * @param fileName File name
     * @return Loaded object or null if error
     */
    @SuppressWarnings("unchecked")
    private <T> T loadObjectFromFile(String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                return null;
            }
            
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            T object = (T) ois.readObject();
            ois.close();
            fis.close();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading object from file: " + fileName, e);
            return null;
        }
    }
    
    /**
     * Check if learning data exists
     * @return True if exists
     */
    public boolean learningDataExists() {
        return preferences.contains(KEY_LAST_SAVE_TIME);
    }
    
    /**
     * Delete all saved learning data
     * @return Success status
     */
    public boolean clearLearningData() {
        Log.i(TAG, "Clearing all learning data");
        
        boolean success = true;
        
        // Delete files
        success &= deleteFile(PHYSICS_KNOWLEDGE_FILE);
        success &= deleteFile(MATH_KNOWLEDGE_FILE);
        success &= deleteFile(CHEMISTRY_KNOWLEDGE_FILE);
        success &= deleteFile(EMOTIONAL_STATE_FILE);
        success &= deleteFile(LEARNING_HISTORY_FILE);
        success &= deleteFile(PREFERENCES_FILE);
        
        // Clear preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        
        Log.i(TAG, "Learning data cleared: " + success);
        return success;
    }
    
    /**
     * Delete a file
     * @param fileName File name
     * @return Success status
     */
    private boolean deleteFile(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
    
    /**
     * Class representing saved learning data
     */
    public static class LearningData {
        private Map<String, Object> physicsKnowledge;
        private Map<String, Object> mathKnowledge;
        private Map<String, Object> chemistryKnowledge;
        private Object emotionalState;
        private Object learningHistory;
        private Map<String, Float> preferences;
        private int learningCount;
        
        /**
         * Constructor
         */
        public LearningData() {
            this.physicsKnowledge = new HashMap<>();
            this.mathKnowledge = new HashMap<>();
            this.chemistryKnowledge = new HashMap<>();
            this.preferences = new HashMap<>();
            this.learningCount = 0;
        }
        
        // Getters and setters
        
        public Map<String, Object> getPhysicsKnowledge() {
            return physicsKnowledge;
        }
        
        public void setPhysicsKnowledge(Map<String, Object> physicsKnowledge) {
            this.physicsKnowledge = physicsKnowledge;
        }
        
        public Map<String, Object> getMathKnowledge() {
            return mathKnowledge;
        }
        
        public void setMathKnowledge(Map<String, Object> mathKnowledge) {
            this.mathKnowledge = mathKnowledge;
        }
        
        public Map<String, Object> getChemistryKnowledge() {
            return chemistryKnowledge;
        }
        
        public void setChemistryKnowledge(Map<String, Object> chemistryKnowledge) {
            this.chemistryKnowledge = chemistryKnowledge;
        }
        
        public Object getEmotionalState() {
            return emotionalState;
        }
        
        public void setEmotionalState(Object emotionalState) {
            this.emotionalState = emotionalState;
        }
        
        public Object getLearningHistory() {
            return learningHistory;
        }
        
        public void setLearningHistory(Object learningHistory) {
            this.learningHistory = learningHistory;
        }
        
        public Map<String, Float> getPreferences() {
            return preferences;
        }
        
        public void setPreferences(Map<String, Float> preferences) {
            this.preferences = preferences;
        }
        
        public int getLearningCount() {
            return learningCount;
        }
        
        public void setLearningCount(int learningCount) {
            this.learningCount = learningCount;
        }
    }
}
