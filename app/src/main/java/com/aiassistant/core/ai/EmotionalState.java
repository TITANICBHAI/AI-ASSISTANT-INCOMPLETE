package com.aiassistant.core.ai;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents the emotional state of the AI, tracking multiple emotional dimensions
 * and their current values to inform behavior and responses.
 */
public class EmotionalState {
    // Emotional dimensions and their values (0.0 to 1.0)
    private Map<String, Double> emotions = new HashMap<>();
    
    // Basic emotional dimensions
    public static final String HAPPINESS = "happiness";
    public static final String SADNESS = "sadness";
    public static final String EXCITEMENT = "excitement";
    public static final String ANXIETY = "anxiety";
    public static final String CURIOSITY = "curiosity";
    public static final String CONFUSION = "confusion";
    public static final String EMPATHY = "empathy";
    
    // The current dominant emotion
    public String dominantEmotion = CURIOSITY;
    
    /**
     * Create a new emotional state with default values
     */
    public EmotionalState() {
        // Initialize with default values
        emotions.put(HAPPINESS, 0.5);
        emotions.put(SADNESS, 0.1);
        emotions.put(EXCITEMENT, 0.3);
        emotions.put(ANXIETY, 0.2);
        emotions.put(CURIOSITY, 0.7);
        emotions.put(CONFUSION, 0.1);
        emotions.put(EMPATHY, 0.6);
        
        updateDominantEmotion();
    }
    
    /**
     * Adjust a specific emotion by the given amount
     */
    public void adjustEmotion(String emotion, double amount) {
        if (!emotions.containsKey(emotion)) {
            emotions.put(emotion, Math.max(0.0, Math.min(1.0, amount)));
        } else {
            double current = emotions.get(emotion);
            emotions.put(emotion, Math.max(0.0, Math.min(1.0, current + amount)));
        }
        updateDominantEmotion();
    }
    
    /**
     * Get the current value of an emotion
     */
    public double getEmotion(String emotion) {
        return emotions.getOrDefault(emotion, 0.0);
    }
    
    /**
     * Set the value of an emotion directly
     */
    public void setEmotion(String emotion, double value) {
        emotions.put(emotion, Math.max(0.0, Math.min(1.0, value)));
        updateDominantEmotion();
    }
    
    /**
     * Reset all emotions to moderate values
     */
    public void resetEmotions() {
        for (String emotion : emotions.keySet()) {
            emotions.put(emotion, 0.5);
        }
        updateDominantEmotion();
    }
    
    /**
     * Update the dominant emotion based on current values
     */
    private void updateDominantEmotion() {
        String strongest = null;
        double maxValue = -1;
        
        for (Map.Entry<String, Double> entry : emotions.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                strongest = entry.getKey();
            }
        }
        
        if (strongest != null) {
            dominantEmotion = strongest;
        }
    }
    
    /**
     * Get a description of the current emotional state
     */
    public String getEmotionalDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Currently feeling: ").append(dominantEmotion);
        
        if (getEmotion(dominantEmotion) > 0.7) {
            description.append(" strongly");
        } else if (getEmotion(dominantEmotion) > 0.3) {
            description.append(" moderately");
        } else {
            description.append(" slightly");
        }
        
        return description.toString();
    }
    
    /**
     * Convert to JSON for storage
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        
        // Store all emotions
        JSONObject emotionsJson = new JSONObject();
        for (Map.Entry<String, Double> entry : emotions.entrySet()) {
            emotionsJson.put(entry.getKey(), entry.getValue());
        }
        json.put("emotions", emotionsJson);
        json.put("dominantEmotion", dominantEmotion);
        
        return json;
    }
    
    /**
     * Create from JSON
     */
    public static EmotionalState fromJSON(JSONObject json) throws JSONException {
        EmotionalState state = new EmotionalState();
        
        // Load all emotions
        JSONObject emotionsJson = json.getJSONObject("emotions");
        Iterator<String> keys = emotionsJson.keys();
        while (keys.hasNext()) {
            String emotion = keys.next();
            state.emotions.put(emotion, emotionsJson.getDouble(emotion));
        }
        
        state.dominantEmotion = json.getString("dominantEmotion");
        
        return state;
    }
}
