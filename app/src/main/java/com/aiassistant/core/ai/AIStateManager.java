package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the AI's state including emotional state, context, and current activity
 * Uses singleton pattern to ensure state consistency across the application
 */
public class AIStateManager {
    private static final String TAG = "AIStateManager";
    
    // Singleton instance
    private static volatile AIStateManager instance;
    
    // Application context
    private final Context context;
    
    // Current emotional state
    private Map<String, Float> currentEmotions;
    private float emotionalValence; // -1.0 to 1.0 (negative to positive)
    private float emotionalArousal; // 0.0 to 1.0 (calm to excited)
    
    // Current context and activity
    private String currentContext;
    private String currentActivity;
    
    // State flags
    private boolean isListening;
    private boolean isProcessing;
    private boolean isResponding;
    
    // State history
    private final Map<String, String> stateHistory;
    private final int maxHistorySize = 100;
    
    /**
     * Private constructor for singleton
     * @param context Application context
     */
    private AIStateManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentEmotions = new HashMap<>();
        this.emotionalValence = 0.0f; // Neutral
        this.emotionalArousal = 0.0f; // Calm
        this.currentContext = "idle";
        this.currentActivity = "none";
        this.isListening = false;
        this.isProcessing = false;
        this.isResponding = false;
        this.stateHistory = new HashMap<>();
        
        Log.d(TAG, "AIStateManager initialized");
    }
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return AIStateManager instance
     */
    public static synchronized AIStateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AIStateManager(context);
        }
        return instance;
    }
    
    /**
     * Reset emotional state to neutral
     */
    public void resetEmotionalState() {
        currentEmotions.clear();
        emotionalValence = 0.0f;
        emotionalArousal = 0.0f;
        
        // Add neutral baseline
        currentEmotions.put("neutral", 1.0f);
        
        Log.d(TAG, "Emotional state reset to neutral");
        addStateHistory("emotion", "reset_to_neutral");
    }
    
    /**
     * Update emotional state by adding an emotion
     * @param emotion Emotion name
     * @param strength Emotion strength (0.0 to 1.0)
     */
    public void updateEmotionalState(String emotion, float strength) {
        if (emotion == null || emotion.isEmpty()) {
            return;
        }
        
        // Clamp strength to valid range
        strength = Math.max(0.0f, Math.min(1.0f, strength));
        
        // Update emotion map
        currentEmotions.put(emotion.toLowerCase(), strength);
        
        // Update valence based on emotion type
        updateValenceArousal(emotion, strength);
        
        Log.d(TAG, "Emotional state updated: " + emotion + " = " + strength);
        addStateHistory("emotion_update", emotion + ":" + strength);
    }
    
    /**
     * Update valence and arousal based on emotion
     * @param emotion Emotion name
     * @param strength Emotion strength
     */
    private void updateValenceArousal(String emotion, float strength) {
        String lowerEmotion = emotion.toLowerCase();
        
        // Define valence modifiers
        Map<String, Float> valenceMap = new HashMap<>();
        valenceMap.put("joy", 1.0f);
        valenceMap.put("happy", 0.9f);
        valenceMap.put("excitement", 0.8f);
        valenceMap.put("love", 0.9f);
        valenceMap.put("contentment", 0.7f);
        valenceMap.put("neutral", 0.0f);
        valenceMap.put("surprise", 0.2f);
        valenceMap.put("confusion", -0.3f);
        valenceMap.put("fear", -0.7f);
        valenceMap.put("anger", -0.8f);
        valenceMap.put("rage", -0.9f);
        valenceMap.put("sadness", -0.6f);
        valenceMap.put("grief", -0.9f);
        valenceMap.put("disgust", -0.7f);
        
        // Define arousal modifiers
        Map<String, Float> arousalMap = new HashMap<>();
        arousalMap.put("joy", 0.7f);
        arousalMap.put("happy", 0.6f);
        arousalMap.put("excitement", 0.9f);
        arousalMap.put("love", 0.6f);
        arousalMap.put("contentment", 0.3f);
        arousalMap.put("neutral", 0.2f);
        arousalMap.put("surprise", 0.8f);
        arousalMap.put("confusion", 0.5f);
        arousalMap.put("fear", 0.8f);
        arousalMap.put("anger", 0.8f);
        arousalMap.put("rage", 0.9f);
        arousalMap.put("sadness", 0.4f);
        arousalMap.put("grief", 0.5f);
        arousalMap.put("disgust", 0.5f);
        
        // Get modifiers for this emotion, default to neutral if not mapped
        float valenceModifier = valenceMap.getOrDefault(lowerEmotion, 0.0f);
        float arousalModifier = arousalMap.getOrDefault(lowerEmotion, 0.2f);
        
        // Apply strength to modifiers
        float valenceChange = valenceModifier * strength;
        float arousalChange = arousalModifier * strength;
        
        // Update emotional valence, weighted blend with current state
        emotionalValence = (emotionalValence * 0.7f) + (valenceChange * 0.3f);
        // Clamp to valid range
        emotionalValence = Math.max(-1.0f, Math.min(1.0f, emotionalValence));
        
        // Update emotional arousal, weighted blend with current state
        emotionalArousal = (emotionalArousal * 0.7f) + (arousalChange * 0.3f);
        // Clamp to valid range
        emotionalArousal = Math.max(0.0f, Math.min(1.0f, emotionalArousal));
    }
    
    /**
     * Get current emotions
     * @return Map of emotion names to strengths
     */
    public Map<String, Float> getCurrentEmotions() {
        return new HashMap<>(currentEmotions);
    }
    
    /**
     * Get emotional valence
     * @return Emotional valence value (-1.0 to 1.0)
     */
    public float getEmotionalValence() {
        return emotionalValence;
    }
    
    /**
     * Set emotional valence
     * @param valence Emotional valence value (-1.0 to 1.0)
     */
    public void setEmotionalValence(float valence) {
        // Clamp to valid range
        this.emotionalValence = Math.max(-1.0f, Math.min(1.0f, valence));
        addStateHistory("valence_set", String.valueOf(this.emotionalValence));
    }
    
    /**
     * Get emotional arousal
     * @return Emotional arousal value (0.0 to 1.0)
     */
    public float getEmotionalArousal() {
        return emotionalArousal;
    }
    
    /**
     * Set emotional arousal
     * @param arousal Emotional arousal value (0.0 to 1.0)
     */
    public void setEmotionalArousal(float arousal) {
        // Clamp to valid range
        this.emotionalArousal = Math.max(0.0f, Math.min(1.0f, arousal));
        addStateHistory("arousal_set", String.valueOf(this.emotionalArousal));
    }
    
    /**
     * Get current context
     * @return Current context
     */
    public String getCurrentContext() {
        return currentContext;
    }
    
    /**
     * Set current context
     * @param context Context name
     */
    public void setCurrentContext(String context) {
        if (context != null && !context.equals(this.currentContext)) {
            this.currentContext = context;
            Log.d(TAG, "Context set to: " + context);
            addStateHistory("context_change", context);
        }
    }
    
    /**
     * Get current activity
     * @return Current activity
     */
    public String getCurrentActivity() {
        return currentActivity;
    }
    
    /**
     * Set current activity
     * @param activity Activity name
     */
    public void setCurrentActivity(String activity) {
        if (activity != null && !activity.equals(this.currentActivity)) {
            this.currentActivity = activity;
            Log.d(TAG, "Activity set to: " + activity);
            addStateHistory("activity_change", activity);
        }
    }
    
    /**
     * Check if AI is in listening state
     * @return True if listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Set listening state
     * @param listening Listening state
     */
    public void setListening(boolean listening) {
        if (this.isListening != listening) {
            this.isListening = listening;
            Log.d(TAG, "Listening state set to: " + listening);
            addStateHistory("listening", String.valueOf(listening));
        }
    }
    
    /**
     * Check if AI is in processing state
     * @return True if processing
     */
    public boolean isProcessing() {
        return isProcessing;
    }
    
    /**
     * Set processing state
     * @param processing Processing state
     */
    public void setProcessing(boolean processing) {
        if (this.isProcessing != processing) {
            this.isProcessing = processing;
            Log.d(TAG, "Processing state set to: " + processing);
            addStateHistory("processing", String.valueOf(processing));
        }
    }
    
    /**
     * Check if AI is in responding state
     * @return True if responding
     */
    public boolean isResponding() {
        return isResponding;
    }
    
    /**
     * Set responding state
     * @param responding Responding state
     */
    public void setResponding(boolean responding) {
        if (this.isResponding != responding) {
            this.isResponding = responding;
            Log.d(TAG, "Responding state set to: " + responding);
            addStateHistory("responding", String.valueOf(responding));
        }
    }
    
    /**
     * Add state change to history
     * @param key State key
     * @param value State value
     */
    private void addStateHistory(String key, String value) {
        // Generate timestamped key
        String timeKey = System.currentTimeMillis() + ":" + key;
        
        // Add to history
        stateHistory.put(timeKey, value);
        
        // Trim history if needed
        if (stateHistory.size() > maxHistorySize) {
            // Find oldest key
            String oldestKey = stateHistory.keySet().stream()
                    .sorted()
                    .findFirst()
                    .orElse(null);
            
            // Remove oldest entry
            if (oldestKey != null) {
                stateHistory.remove(oldestKey);
            }
        }
    }
    
    /**
     * Get state history
     * @return State history map
     */
    public Map<String, String> getStateHistory() {
        return new HashMap<>(stateHistory);
    }
    
    /**
     * Get dominant emotion
     * @return Dominant emotion name
     */
    public String getDominantEmotion() {
        if (currentEmotions.isEmpty()) {
            return "neutral";
        }
        
        String dominant = "neutral";
        float maxStrength = 0.0f;
        
        for (Map.Entry<String, Float> entry : currentEmotions.entrySet()) {
            if (entry.getValue() > maxStrength) {
                maxStrength = entry.getValue();
                dominant = entry.getKey();
            }
        }
        
        return dominant;
    }
    
    /**
     * Check if emotional state is positive
     * @return True if positive
     */
    public boolean isEmotionalStatePositive() {
        return emotionalValence > 0.3f;
    }
    
    /**
     * Check if emotional state is negative
     * @return True if negative
     */
    public boolean isEmotionalStateNegative() {
        return emotionalValence < -0.3f;
    }
    
    /**
     * Check if emotional arousal is high
     * @return True if arousal is high
     */
    public boolean isEmotionalArousalHigh() {
        return emotionalArousal > 0.7f;
    }
    
    /**
     * Convert emotional state to human-readable string
     * @return String representation of emotional state
     */
    public String getEmotionalStateString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dominant: ").append(getDominantEmotion());
        sb.append(", Valence: ").append(String.format("%.2f", emotionalValence));
        sb.append(", Arousal: ").append(String.format("%.2f", emotionalArousal));
        
        return sb.toString();
    }
}
