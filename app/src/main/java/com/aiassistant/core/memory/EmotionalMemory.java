package com.aiassistant.core.memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Emotional memory implementation
 * Tracks and manages emotional history of interactions
 */
public class EmotionalMemory {
    private static final String TAG = "EmotionalMemory";
    
    // SharedPreferences file name
    private static final String PREFS_NAME = "ai_emotional_memory";
    
    // Memory keys
    private static final String KEY_EMOTION_HISTORY = "emotion_history";
    private static final String KEY_EMOTION_STATS = "emotion_stats";
    
    // Maximum history size
    private static final int MAX_HISTORY_SIZE = 100;
    
    // Context for accessing SharedPreferences
    private final Context context;
    
    // Gson for serialization
    private final Gson gson;
    
    // Background thread executor
    private final Executor executor;
    
    // Recent emotional states (most recent first)
    private final Queue<EmotionalEvent> emotionHistory;
    
    // Emotional statistics (counts by emotion)
    private Map<String, Integer> emotionStats;
    
    /**
     * Constructor
     * @param context Application context
     */
    public EmotionalMemory(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.emotionHistory = new LinkedList<>();
        
        // Load emotional memory
        loadEmotionalMemory();
        
        Log.d(TAG, "Emotional memory initialized with " + emotionHistory.size() + " entries");
    }
    
    /**
     * Record an emotional event
     * @param emotion Emotion name
     * @param intensity Intensity (0.0-1.0)
     * @param context Optional context information
     */
    public void recordEmotion(String emotion, float intensity, String context) {
        // Create emotional event
        EmotionalEvent event = new EmotionalEvent(emotion, intensity, context);
        
        // Add to history
        emotionHistory.add(event);
        
        // Trim history if needed
        while (emotionHistory.size() > MAX_HISTORY_SIZE) {
            emotionHistory.poll();
        }
        
        // Update statistics
        int count = emotionStats.getOrDefault(emotion, 0);
        emotionStats.put(emotion, count + 1);
        
        // Save to SharedPreferences
        saveEmotionalMemory();
        
        Log.d(TAG, "Recorded emotional event: " + emotion + " (" + intensity + ")");
    }
    
    /**
     * Record an emotional event (without context)
     * @param emotion Emotion name
     * @param intensity Intensity (0.0-1.0)
     */
    public void recordEmotion(String emotion, float intensity) {
        recordEmotion(emotion, intensity, null);
    }
    
    /**
     * Get recent emotional history
     * @param limit Maximum number of entries to return
     * @return List of recent emotional events (most recent first)
     */
    public List<EmotionalEvent> getRecentHistory(int limit) {
        List<EmotionalEvent> result = new ArrayList<>(emotionHistory);
        
        // Limit result size
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        
        return result;
    }
    
    /**
     * Get all emotional history
     * @return List of all emotional events (most recent first)
     */
    public List<EmotionalEvent> getAllHistory() {
        return new ArrayList<>(emotionHistory);
    }
    
    /**
     * Get emotional statistics (counts by emotion)
     * @return Map of emotion names to occurrence counts
     */
    public Map<String, Integer> getEmotionStats() {
        return new HashMap<>(emotionStats);
    }
    
    /**
     * Get dominant emotion (most frequent)
     * @return Dominant emotion name
     */
    public String getDominantEmotion() {
        String dominantEmotion = "neutral";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : emotionStats.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantEmotion = entry.getKey();
            }
        }
        
        return dominantEmotion;
    }
    
    /**
     * Get top emotions by frequency
     * @param count Number of top emotions to return
     * @return List of top emotions in descending order of frequency
     */
    public List<Map.Entry<String, Integer>> getTopEmotions(int count) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(emotionStats.entrySet());
        
        // Sort by count (descending)
        Collections.sort(entries, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        // Limit result size
        if (entries.size() > count) {
            entries = entries.subList(0, count);
        }
        
        return entries;
    }
    
    /**
     * Clear emotional history
     */
    public void clearHistory() {
        emotionHistory.clear();
        saveEmotionalMemory();
        Log.d(TAG, "Cleared emotional history");
    }
    
    /**
     * Reset emotional statistics
     */
    public void resetStats() {
        emotionStats.clear();
        saveEmotionalMemory();
        Log.d(TAG, "Reset emotional statistics");
    }
    
    /**
     * Clear all emotional memory
     */
    public void clear() {
        emotionHistory.clear();
        emotionStats.clear();
        saveEmotionalMemory();
        Log.d(TAG, "Cleared all emotional memory");
    }
    
    /**
     * Get emotional events by emotion
     * @param emotion Emotion name
     * @return List of emotional events with matching emotion
     */
    public List<EmotionalEvent> getEventsByEmotion(String emotion) {
        List<EmotionalEvent> result = new ArrayList<>();
        
        for (EmotionalEvent event : emotionHistory) {
            if (event.getEmotion().equals(emotion)) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    /**
     * Get emotional events by context
     * @param context Context pattern
     * @return List of emotional events with matching context
     */
    public List<EmotionalEvent> getEventsByContext(String context) {
        List<EmotionalEvent> result = new ArrayList<>();
        
        for (EmotionalEvent event : emotionHistory) {
            if (event.getContext() != null && event.getContext().contains(context)) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    /**
     * Load emotional memory from SharedPreferences
     */
    private void loadEmotionalMemory() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Get JSON strings
        String historyJson = prefs.getString(KEY_EMOTION_HISTORY, "[]");
        String statsJson = prefs.getString(KEY_EMOTION_STATS, "{}");
        
        // Parse JSON
        Type historyType = new TypeToken<List<EmotionalEvent>>(){}.getType();
        Type statsType = new TypeToken<HashMap<String, Integer>>(){}.getType();
        
        try {
            List<EmotionalEvent> history = gson.fromJson(historyJson, historyType);
            
            // Add to queue
            emotionHistory.clear();
            if (history != null) {
                emotionHistory.addAll(history);
            }
            
            // Load stats
            emotionStats = gson.fromJson(statsJson, statsType);
            
            // Initialize if null
            if (emotionStats == null) {
                emotionStats = new HashMap<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading emotional memory: " + e.getMessage());
            emotionStats = new HashMap<>();
        }
    }
    
    /**
     * Save emotional memory to SharedPreferences
     */
    private void saveEmotionalMemory() {
        // Save on background thread
        executor.execute(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                
                // Convert to JSON
                String historyJson = gson.toJson(new ArrayList<>(emotionHistory));
                String statsJson = gson.toJson(emotionStats);
                
                // Save to SharedPreferences
                editor.putString(KEY_EMOTION_HISTORY, historyJson);
                editor.putString(KEY_EMOTION_STATS, statsJson);
                editor.apply();
                
                Log.d(TAG, "Saved emotional memory to SharedPreferences");
            } catch (Exception e) {
                Log.e(TAG, "Error saving emotional memory: " + e.getMessage());
            }
        });
    }
    
    /**
     * Emotional event class
     * Represents an emotional state at a point in time
     */
    public static class EmotionalEvent {
        private final String emotion;
        private final float intensity;
        private final String context;
        private final long timestamp;
        
        /**
         * Constructor
         * @param emotion Emotion name
         * @param intensity Intensity (0.0-1.0)
         * @param context Optional context information
         */
        public EmotionalEvent(String emotion, float intensity, String context) {
            this.emotion = emotion;
            this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
            this.context = context;
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Get emotion name
         * @return Emotion name
         */
        public String getEmotion() {
            return emotion;
        }
        
        /**
         * Get intensity
         * @return Intensity (0.0-1.0)
         */
        public float getIntensity() {
            return intensity;
        }
        
        /**
         * Get context information
         * @return Context information
         */
        public String getContext() {
            return context;
        }
        
        /**
         * Get timestamp
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
