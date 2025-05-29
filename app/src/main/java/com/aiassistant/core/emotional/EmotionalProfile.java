package com.aiassistant.core.emotional;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the emotional profile of a caller, tracking their emotional patterns
 * over time to help the AI better respond to them in future calls.
 */
public class EmotionalProfile {
    private static final String TAG = "EmotionalProfile";
    private static final int MAX_HISTORY_SIZE = 20;  // Maximum number of emotional events to track
    
    // Caller identifier (phone number, contact name, etc.)
    private String callerId;
    
    // Last detected emotion
    private EmotionState lastEmotion = EmotionState.NEUTRAL;
    private float lastIntensity = 0.0f;
    private long lastTimestamp = 0;
    
    // Current interaction tracking
    private long currentInteractionStartTime = 0;
    
    // Maps each emotion to its frequency count
    private final Map<EmotionState, Integer> emotionFrequency = new HashMap<>();
    
    // Maps each emotion to its average intensity (0.0-1.0)
    private final Map<EmotionState, Float> emotionIntensity = new HashMap<>();
    
    // History of emotional events, from most recent to oldest
    private final Map<Long, EmotionalEvent> emotionalHistory = new HashMap<>();
    
    /**
     * Constructor for a new emotional profile
     */
    public EmotionalProfile(String callerId) {
        this.callerId = callerId;
        
        // Initialize frequency and intensity maps
        for (EmotionState emotion : EmotionState.values()) {
            emotionFrequency.put(emotion, 0);
            emotionIntensity.put(emotion, 0.0f);
        }
    }
    
    /**
     * Default constructor
     */
    public EmotionalProfile() {
        this("unknown");
    }
    
    /**
     * Get the caller's ID
     */
    public String getCallerId() {
        return callerId;
    }
    
    /**
     * Set the caller ID
     */
    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }
    
    /**
     * Record the start of an emotional interaction (like a call)
     */
    public void recordInteractionStart() {
        currentInteractionStartTime = System.currentTimeMillis();
        Log.d(TAG, "Started new emotional interaction at " + new Date(currentInteractionStartTime));
    }
    
    /**
     * Record the end of an emotional interaction
     */
    public void recordInteractionEnd() {
        if (currentInteractionStartTime == 0) {
            return; // No interaction was started
        }
        
        long interactionDuration = System.currentTimeMillis() - currentInteractionStartTime;
        Log.d(TAG, "Ended emotional interaction, duration: " + (interactionDuration / 1000) + " seconds");
        
        // Reset the interaction tracking
        currentInteractionStartTime = 0;
    }
    
    /**
     * Record a detected emotion during an interaction
     */
    public void recordEmotion(EmotionState emotion, float intensity) {
        updateEmotion(emotion, intensity);
    }
    
    /**
     * Update the profile with a newly detected emotion
     */
    public void updateEmotion(EmotionState emotion, float intensity) {
        long currentTime = System.currentTimeMillis();
        
        // Update the last emotion data
        lastEmotion = emotion;
        lastIntensity = intensity;
        lastTimestamp = currentTime;
        
        // Update frequency counts
        int currentCount = emotionFrequency.getOrDefault(emotion, 0);
        emotionFrequency.put(emotion, currentCount + 1);
        
        // Update average intensity
        float currentIntensity = emotionIntensity.getOrDefault(emotion, 0.0f);
        float newIntensity = (currentIntensity * currentCount + intensity) / (currentCount + 1);
        emotionIntensity.put(emotion, newIntensity);
        
        // Add to history
        emotionalHistory.put(currentTime, new EmotionalEvent(emotion, intensity, currentTime));
        
        // Prune history if it gets too large
        pruneHistory();
    }
    
    /**
     * Get the most common emotion for this caller
     */
    public EmotionState getMostCommonEmotion() {
        EmotionState mostCommon = EmotionState.NEUTRAL;
        int highestCount = 0;
        
        for (Map.Entry<EmotionState, Integer> entry : emotionFrequency.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }
        
        return mostCommon;
    }
    
    /**
     * Check if this caller tends to express strong emotions
     */
    public boolean hasStrongEmotions() {
        float totalIntensity = 0.0f;
        int count = 0;
        
        for (Map.Entry<EmotionState, Float> entry : emotionIntensity.entrySet()) {
            if (entry.getKey() != EmotionState.NEUTRAL) {
                totalIntensity += entry.getValue();
                count++;
            }
        }
        
        return count > 0 && (totalIntensity / count) > 0.6f;
    }
    
    /**
     * Get the last detected emotion
     */
    public EmotionState getLastEmotion() {
        return lastEmotion;
    }
    
    /**
     * Get the intensity of the last detected emotion
     */
    public float getLastIntensity() {
        return lastIntensity;
    }
    
    /**
     * Get the timestamp of the last emotional update
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }
    
    /**
     * Check if this caller tends to express negative emotions
     */
    public boolean hasNegativeBias() {
        int negativeCount = 0;
        int positiveCount = 0;
        
        for (Map.Entry<EmotionState, Integer> entry : emotionFrequency.entrySet()) {
            if (entry.getKey().isNegative()) {
                negativeCount += entry.getValue();
            } else if (entry.getKey().isPositive()) {
                positiveCount += entry.getValue();
            }
        }
        
        return negativeCount > positiveCount * 1.5;
    }
    
    /**
     * Check if this caller tends to express positive emotions
     */
    public boolean hasPositiveBias() {
        int negativeCount = 0;
        int positiveCount = 0;
        
        for (Map.Entry<EmotionState, Integer> entry : emotionFrequency.entrySet()) {
            if (entry.getKey().isNegative()) {
                negativeCount += entry.getValue();
            } else if (entry.getKey().isPositive()) {
                positiveCount += entry.getValue();
            }
        }
        
        return positiveCount > negativeCount * 1.5;
    }
    
    /**
     * Get the frequency of a specific emotion
     */
    public int getEmotionFrequency(EmotionState emotion) {
        return emotionFrequency.getOrDefault(emotion, 0);
    }
    
    /**
     * Get the average intensity of a specific emotion
     */
    public float getEmotionIntensity(EmotionState emotion) {
        return emotionIntensity.getOrDefault(emotion, 0.0f);
    }
    
    /**
     * Remove old entries from the history to maintain a reasonable size
     */
    private void pruneHistory() {
        if (emotionalHistory.size() <= MAX_HISTORY_SIZE) {
            return;
        }
        
        // Get the oldest timestamp to remove
        long oldestToKeep = Long.MAX_VALUE;
        int count = 0;
        for (Long timestamp : emotionalHistory.keySet()) {
            if (count < emotionalHistory.size() - MAX_HISTORY_SIZE) {
                if (timestamp < oldestToKeep) {
                    oldestToKeep = timestamp;
                }
            }
            count++;
        }
        
        // Remove all entries older than the cutoff
        emotionalHistory.entrySet().removeIf(entry -> entry.getKey() < oldestToKeep);
    }
    
    /**
     * Serialize the profile to a JSON string for storage
     */
    public String serialize() {
        JSONObject json = new JSONObject();
        try {
            json.put("callerId", callerId);
            json.put("lastEmotion", lastEmotion.name());
            json.put("lastIntensity", lastIntensity);
            json.put("lastTimestamp", lastTimestamp);
            
            // Serialize emotion frequencies
            JSONObject frequencyJson = new JSONObject();
            for (Map.Entry<EmotionState, Integer> entry : emotionFrequency.entrySet()) {
                frequencyJson.put(entry.getKey().name(), entry.getValue());
            }
            json.put("frequencies", frequencyJson);
            
            // Serialize emotion intensities
            JSONObject intensityJson = new JSONObject();
            for (Map.Entry<EmotionState, Float> entry : emotionIntensity.entrySet()) {
                intensityJson.put(entry.getKey().name(), entry.getValue());
            }
            json.put("intensities", intensityJson);
            
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error serializing emotional profile", e);
            return "{}";
        }
    }
    
    /**
     * Deserialize a profile from a JSON string
     */
    public static EmotionalProfile deserialize(String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            String callerId = jsonObj.optString("callerId", "unknown");
            EmotionalProfile profile = new EmotionalProfile(callerId);
            
            profile.lastEmotion = EmotionState.valueOf(jsonObj.optString("lastEmotion", "NEUTRAL"));
            profile.lastIntensity = (float) jsonObj.optDouble("lastIntensity", 0.0);
            profile.lastTimestamp = jsonObj.optLong("lastTimestamp", 0);
            
            // Deserialize frequencies
            JSONObject frequencyJson = jsonObj.optJSONObject("frequencies");
            if (frequencyJson != null) {
                for (EmotionState emotion : EmotionState.values()) {
                    if (frequencyJson.has(emotion.name())) {
                        profile.emotionFrequency.put(emotion, frequencyJson.getInt(emotion.name()));
                    }
                }
            }
            
            // Deserialize intensities
            JSONObject intensityJson = jsonObj.optJSONObject("intensities");
            if (intensityJson != null) {
                for (EmotionState emotion : EmotionState.values()) {
                    if (intensityJson.has(emotion.name())) {
                        profile.emotionIntensity.put(emotion, (float) intensityJson.getDouble(emotion.name()));
                    }
                }
            }
            
            return profile;
        } catch (JSONException e) {
            Log.e(TAG, "Error deserializing emotional profile", e);
            return new EmotionalProfile();
        }
    }
    
    /**
     * Get average emotional intensity across all emotions
     */
    public float getAverageEmotionalIntensity() {
        float totalIntensity = 0;
        int count = 0;
        
        for (Map.Entry<EmotionState, Float> entry : emotionIntensity.entrySet()) {
            if (entry.getKey() != EmotionState.NEUTRAL && entry.getValue() > 0) {
                totalIntensity += entry.getValue();
                count++;
            }
        }
        
        return count > 0 ? totalIntensity / count : 0;
    }
    
    /**
     * Get the predominant emotion (most frequent non-neutral emotion)
     */
    public EmotionState getPredominantEmotion() {
        EmotionState predominant = EmotionState.NEUTRAL;
        int highestCount = 0;
        
        for (Map.Entry<EmotionState, Integer> entry : emotionFrequency.entrySet()) {
            if (entry.getKey() != EmotionState.NEUTRAL && entry.getValue() > highestCount) {
                highestCount = entry.getValue();
                predominant = entry.getKey();
            }
        }
        
        return predominant;
    }
    
    /**
     * Inner class to represent an emotional event in the caller's history
     */
    private static class EmotionalEvent {
        private final EmotionState emotion;
        private final float intensity;
        private final long timestamp;
        
        public EmotionalEvent(EmotionState emotion, float intensity, long timestamp) {
            this.emotion = emotion;
            this.intensity = intensity;
            this.timestamp = timestamp;
        }
        
        public EmotionState getEmotion() {
            return emotion;
        }
        
        public float getIntensity() {
            return intensity;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
