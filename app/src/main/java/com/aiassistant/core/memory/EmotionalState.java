package com.aiassistant.core.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the emotional state of the AI
 * Contains current emotions and their intensities
 */
public class EmotionalState {
    // Current emotion levels (0.0-1.0)
    public final Map<String, Float> emotionLevels = new HashMap<>();
    
    // Dominant emotion
    public String dominantEmotion = "neutral";
    
    // Dominant emotion intensity
    public float dominantIntensity = 0.0f;
    
    /**
     * Constructor with default neutral state
     */
    public EmotionalState() {
        // Initialize with basic emotions
        emotionLevels.put("joy", 0.0f);
        emotionLevels.put("sadness", 0.0f);
        emotionLevels.put("anger", 0.0f);
        emotionLevels.put("fear", 0.0f);
        emotionLevels.put("surprise", 0.0f);
        emotionLevels.put("disgust", 0.0f);
        emotionLevels.put("neutral", 1.0f);
        emotionLevels.put("concern", 0.0f);
        
        // Set defaults
        dominantEmotion = "neutral";
        dominantIntensity = 1.0f;
    }
    
    /**
     * Copy constructor
     * @param other EmotionalState to copy
     */
    public EmotionalState(EmotionalState other) {
        emotionLevels.putAll(other.emotionLevels);
        dominantEmotion = other.dominantEmotion;
        dominantIntensity = other.dominantIntensity;
    }
    
    /**
     * Set emotion level and update dominant emotion
     * @param emotion Emotion name
     * @param level Intensity level (0.0-1.0)
     */
    public void setEmotionLevel(String emotion, float level) {
        // Ensure level is within range
        level = Math.max(0.0f, Math.min(1.0f, level));
        
        // Set emotion level
        emotionLevels.put(emotion, level);
        
        // Update dominant emotion
        updateDominantEmotion();
    }
    
    /**
     * Adjust an emotion level by adding to current value
     * @param emotion Emotion name
     * @param adjustment Amount to adjust by (can be negative)
     */
    public void adjustEmotionLevel(String emotion, float adjustment) {
        Float currentLevel = emotionLevels.getOrDefault(emotion, 0.0f);
        setEmotionLevel(emotion, currentLevel + adjustment);
    }
    
    /**
     * Get emotion level
     * @param emotion Emotion name
     * @return Intensity level (0.0-1.0)
     */
    public float getEmotionLevel(String emotion) {
        return emotionLevels.getOrDefault(emotion, 0.0f);
    }
    
    /**
     * Decay all emotions toward neutral
     * @param decayRate Rate of decay per call (0.0-1.0)
     */
    public void decayEmotions(float decayRate) {
        // Ensure decay rate is within range
        decayRate = Math.max(0.0f, Math.min(0.5f, decayRate));
        
        // Decay all non-neutral emotions
        for (String emotion : emotionLevels.keySet()) {
            if (!"neutral".equals(emotion)) {
                float currentLevel = emotionLevels.get(emotion);
                float newLevel = Math.max(0.0f, currentLevel - decayRate);
                emotionLevels.put(emotion, newLevel);
            }
        }
        
        // Increase neutral as others decay
        float neutralLevel = emotionLevels.get("neutral");
        emotionLevels.put("neutral", Math.min(1.0f, neutralLevel + decayRate));
        
        // Update dominant emotion
        updateDominantEmotion();
    }
    
    /**
     * Update the dominant emotion based on current levels
     */
    private void updateDominantEmotion() {
        float maxLevel = 0.0f;
        String maxEmotion = "neutral";
        
        for (Map.Entry<String, Float> entry : emotionLevels.entrySet()) {
            if (entry.getValue() > maxLevel) {
                maxLevel = entry.getValue();
                maxEmotion = entry.getKey();
            }
        }
        
        this.dominantEmotion = maxEmotion;
        this.dominantIntensity = maxLevel;
    }
    
    /**
     * Get the emotional state as a string
     * @return String representation of emotional state
     */
    @Override
    public String toString() {
        return String.format("Dominant: %s (%.2f), All: %s", 
                dominantEmotion, dominantIntensity, emotionLevels.toString());
    }
}
