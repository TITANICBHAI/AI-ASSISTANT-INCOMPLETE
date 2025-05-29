package com.aiassistant.core.emotional;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.learning.memory.MemoryStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages emotional intelligence capabilities for the AI assistant.
 * This class is responsible for detecting emotions in conversations,
 * tracking emotional states, and generating emotionally appropriate responses.
 */
public class EmotionalIntelligenceManager {
    private static final String TAG = "EmotionalIntelligence";
    private static final String PREFS_NAME = "emotional_intelligence_prefs";
    private static final String PREF_ENABLED = "emotional_intelligence_enabled";
    private static final String PREF_ADAPTATION_RATE = "emotional_adaptation_rate";
    private static final String PREF_MIMICRY_RATE = "emotional_mimicry_rate";
    private static final String PREF_BASELINE_RATE = "emotional_baseline_rate";
    
    // Singleton instance
    private static EmotionalIntelligenceManager instance;
    
    // Context for accessing resources and preferences
    private final Context context;
    
    // Dependencies
    private final AIStateManager aiStateManager;
    private final MemoryStorage memoryStorage;
    
    // Current emotional state
    private EmotionState currentEmotionalState = EmotionState.NEUTRAL;
    
    // Emotional intensity (0.0-1.0 scale)
    private float currentIntensity = 0.0f;
    
    // Tracks the emotional states of different callers
    private final ConcurrentHashMap<String, EmotionalProfile> callerProfiles = new ConcurrentHashMap<>();
    
    // Configuration settings
    private boolean enabled = true;
    private float adaptationRate = 0.7f;  // How quickly AI adapts to detected emotions (0.0-1.0)
    private float mimicryRate = 0.5f;     // How much AI mirrors caller's emotions (0.0-1.0)
    private float baselineRate = 0.2f;    // How quickly AI returns to neutral state (0.0-1.0)
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private EmotionalIntelligenceManager(Context context, AIStateManager aiStateManager, MemoryStorage memoryStorage) {
        this.context = context.getApplicationContext();
        this.aiStateManager = aiStateManager;
        this.memoryStorage = memoryStorage;
        loadSettings();
    }
    
    /**
     * Get the singleton instance of the EmotionalIntelligenceManager
     */
    public static synchronized EmotionalIntelligenceManager getInstance(Context context, AIStateManager aiStateManager, MemoryStorage memoryStorage) {
        if (instance == null) {
            instance = new EmotionalIntelligenceManager(context, aiStateManager, memoryStorage);
        }
        return instance;
    }
    
    /**
     * Load saved settings from SharedPreferences
     */
    private void loadSettings() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        enabled = prefs.getBoolean(PREF_ENABLED, true);
        adaptationRate = prefs.getFloat(PREF_ADAPTATION_RATE, 0.7f);
        mimicryRate = prefs.getFloat(PREF_MIMICRY_RATE, 0.5f);
        baselineRate = prefs.getFloat(PREF_BASELINE_RATE, 0.2f);
    }
    
    /**
     * Save current settings to SharedPreferences
     */
    public void saveSettings() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_ENABLED, enabled);
        editor.putFloat(PREF_ADAPTATION_RATE, adaptationRate);
        editor.putFloat(PREF_MIMICRY_RATE, mimicryRate);
        editor.putFloat(PREF_BASELINE_RATE, baselineRate);
        editor.apply();
    }
    
    /**
     * Reset settings to default values
     */
    public void resetSettings() {
        enabled = true;
        adaptationRate = 0.7f;
        mimicryRate = 0.5f;
        baselineRate = 0.2f;
        saveSettings();
    }
    
    /**
     * Enable or disable emotional intelligence features
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        saveSettings();
    }
    
    /**
     * Check if emotional intelligence is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the adaptation rate - how quickly the AI adapts to detected emotions
     * @param rate Value between 0.0 and 1.0
     */
    public void setAdaptationRate(float rate) {
        if (rate >= 0.0f && rate <= 1.0f) {
            this.adaptationRate = rate;
            saveSettings();
        }
    }
    
    /**
     * Get the adaptation rate
     */
    public float getAdaptationRate() {
        return adaptationRate;
    }
    
    /**
     * Set the mimicry rate - how much the AI mirrors caller's emotions
     * @param rate Value between 0.0 and 1.0
     */
    public void setMimicryRate(float rate) {
        if (rate >= 0.0f && rate <= 1.0f) {
            this.mimicryRate = rate;
            saveSettings();
        }
    }
    
    /**
     * Get the mimicry rate
     */
    public float getMimicryRate() {
        return mimicryRate;
    }
    
    /**
     * Set the baseline reversion rate - how quickly AI returns to neutral
     * @param rate Value between 0.0 and 1.0
     */
    public void setBaselineRate(float rate) {
        if (rate >= 0.0f && rate <= 1.0f) {
            this.baselineRate = rate;
            saveSettings();
        }
    }
    
    /**
     * Get the baseline reversion rate
     */
    public float getBaselineRate() {
        return baselineRate;
    }
    
    /**
     * Get the current emotional state of the AI
     */
    public EmotionState getCurrentEmotionalState() {
        return currentEmotionalState;
    }
    
    /**
     * Get the intensity of the current emotional state
     */
    public float getCurrentIntensity() {
        return currentIntensity;
    }
    
    /**
     * Detects emotions in the provided text using natural language processing
     * and acoustic cues (if audio is available)
     * 
     * @param text The text to analyze for emotional content
     * @param callerId Identifier for the caller (phone number, contact name, etc.)
     * @return Detected primary emotion and its intensity
     */
    public EmotionDetectionResult detectEmotion(String text, String callerId) {
        if (!enabled) {
            return new EmotionDetectionResult(EmotionState.NEUTRAL, 0.0f);
        }
        
        // Placeholder for NLP-based emotion detection
        // In a real implementation, this would use a trained model to analyze text
        Map<EmotionState, Float> emotionScores = analyzeTextForEmotions(text);
        
        // Find the emotion with the highest score
        EmotionState primaryEmotion = EmotionState.NEUTRAL;
        float maxScore = 0.0f;
        
        for (Map.Entry<EmotionState, Float> entry : emotionScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                primaryEmotion = entry.getKey();
            }
        }
        
        // Store the detected emotion in the caller's profile
        updateCallerEmotionalProfile(callerId, primaryEmotion, maxScore);
        
        return new EmotionDetectionResult(primaryEmotion, maxScore);
    }
    
    /**
     * Analyzes text to detect emotional content
     * This is a simplified implementation and would be replaced with a more sophisticated
     * NLP model in a production implementation
     */
    private Map<EmotionState, Float> analyzeTextForEmotions(String text) {
        Map<EmotionState, Float> scores = new HashMap<>();
        
        // Initialize all emotion scores to 0
        for (EmotionState emotion : EmotionState.values()) {
            scores.put(emotion, 0.0f);
        }
        
        // Simple keyword-based detection (placeholder implementation)
        String lowerText = text.toLowerCase();
        
        // Check for happiness indicators
        if (lowerText.contains("happy") || lowerText.contains("glad") || 
            lowerText.contains("great") || lowerText.contains("excellent") ||
            lowerText.contains("wonderful") || lowerText.contains("thrilled")) {
            scores.put(EmotionState.HAPPY, 0.8f);
        }
        
        // Check for sadness indicators
        if (lowerText.contains("sad") || lowerText.contains("upset") || 
            lowerText.contains("unhappy") || lowerText.contains("depressed") ||
            lowerText.contains("sorry") || lowerText.contains("regret")) {
            scores.put(EmotionState.SAD, 0.7f);
        }
        
        // Check for anger indicators
        if (lowerText.contains("angry") || lowerText.contains("mad") || 
            lowerText.contains("furious") || lowerText.contains("annoyed") ||
            lowerText.contains("irritated") || lowerText.contains("outraged")) {
            scores.put(EmotionState.ANGRY, 0.9f);
        }
        
        // Check for fear indicators
        if (lowerText.contains("afraid") || lowerText.contains("scared") || 
            lowerText.contains("frightened") || lowerText.contains("terrified") ||
            lowerText.contains("worried") || lowerText.contains("anxious")) {
            scores.put(EmotionState.AFRAID, 0.8f);
        }
        
        // Check for surprise indicators
        if (lowerText.contains("surprised") || lowerText.contains("shocked") || 
            lowerText.contains("amazed") || lowerText.contains("astonished") ||
            lowerText.contains("wow") || lowerText.contains("unexpected")) {
            scores.put(EmotionState.SURPRISED, 0.7f);
        }
        
        // Check for confusion indicators
        if (lowerText.contains("confused") || lowerText.contains("unsure") || 
            lowerText.contains("puzzled") || lowerText.contains("perplexed") ||
            lowerText.contains("don't understand") || lowerText.contains("unclear")) {
            scores.put(EmotionState.CONFUSED, 0.6f);
        }
        
        // If no strong emotions were detected, assign a low neutral score
        boolean hasStrongEmotion = false;
        for (Float score : scores.values()) {
            if (score > 0.5f) {
                hasStrongEmotion = true;
                break;
            }
        }
        
        if (!hasStrongEmotion) {
            scores.put(EmotionState.NEUTRAL, 0.9f);
        }
        
        return scores;
    }
    
    /**
     * Updates the AI's emotional state based on detected emotions and settings
     * 
     * @param detectedEmotion The primary emotion detected from the user
     * @param intensity The intensity of the detected emotion (0.0-1.0)
     */
    public void updateEmotionalState(EmotionState detectedEmotion, float intensity) {
        if (!enabled) {
            currentEmotionalState = EmotionState.NEUTRAL;
            currentIntensity = 0.0f;
            return;
        }
        
        // Calculate how much the AI should mirror the detected emotion
        float targetIntensity = intensity * mimicryRate;
        
        // If the detected emotion is negative and strong, respond with appropriate counter-emotion
        if (intensity > 0.7f) {
            switch (detectedEmotion) {
                case ANGRY:
                    // Respond to anger with calm or concern
                    currentEmotionalState = EmotionState.CONCERNED;
                    currentIntensity = targetIntensity;
                    break;
                case AFRAID:
                    // Respond to fear with reassurance
                    currentEmotionalState = EmotionState.REASSURING;
                    currentIntensity = targetIntensity;
                    break;
                default:
                    // For other strong emotions, mirror the emotion but adapt intensity
                    float adaptedIntensity = currentIntensity * (1 - adaptationRate) + targetIntensity * adaptationRate;
                    currentEmotionalState = detectedEmotion;
                    currentIntensity = adaptedIntensity;
                    break;
            }
        } else {
            // For low to moderate intensity emotions, gradually adapt
            float adaptedIntensity = currentIntensity * (1 - adaptationRate) + targetIntensity * adaptationRate;
            
            // If we should mirror the emotion
            if (Math.random() < mimicryRate) {
                currentEmotionalState = detectedEmotion;
            }
            
            currentIntensity = adaptedIntensity;
        }
        
        // Apply baseline reversion - gradually return to neutral state
        if (Math.random() < baselineRate) {
            currentIntensity *= (1.0f - baselineRate);
            
            // If intensity is very low, return to neutral
            if (currentIntensity < 0.2f) {
                currentEmotionalState = EmotionState.NEUTRAL;
                currentIntensity = 0.0f;
            }
        }
        
        Log.d(TAG, "Updated emotional state: " + currentEmotionalState + " with intensity " + currentIntensity);
    }
    
    /**
     * Modifies the given response text to reflect the current emotional state of the AI
     * 
     * @param originalResponse The original response text
     * @return The emotionally adjusted response
     */
    public String adjustResponseForEmotionalState(String originalResponse) {
        if (!enabled || currentEmotionalState == EmotionState.NEUTRAL || currentIntensity < 0.3f) {
            return originalResponse;
        }
        
        String modifiedResponse = originalResponse;
        
        // Apply emotional adjustments based on the current state and intensity
        switch (currentEmotionalState) {
            case HAPPY:
                modifiedResponse = addHappyEmotionalTone(originalResponse, currentIntensity);
                break;
            case SAD:
                modifiedResponse = addSadEmotionalTone(originalResponse, currentIntensity);
                break;
            case ANGRY:
                modifiedResponse = addAngryEmotionalTone(originalResponse, currentIntensity);
                break;
            case AFRAID:
                modifiedResponse = addAfraidEmotionalTone(originalResponse, currentIntensity);
                break;
            case SURPRISED:
                modifiedResponse = addSurprisedEmotionalTone(originalResponse, currentIntensity);
                break;
            case CONFUSED:
                modifiedResponse = addConfusedEmotionalTone(originalResponse, currentIntensity);
                break;
            case CONCERNED:
                modifiedResponse = addConcernedEmotionalTone(originalResponse, currentIntensity);
                break;
            case CURIOUS:
                modifiedResponse = addCuriousEmotionalTone(originalResponse, currentIntensity);
                break;
            case REASSURING:
                modifiedResponse = addReassurringEmotionalTone(originalResponse, currentIntensity);
                break;
            default:
                // No adjustment needed for NEUTRAL
                break;
        }
        
        return modifiedResponse;
    }
    
    /**
     * Updates the emotional profile of a caller based on detected emotions
     */
    private void updateCallerEmotionalProfile(String callerId, EmotionState emotion, float intensity) {
        if (callerId == null || callerId.isEmpty()) {
            return;
        }
        
        EmotionalProfile profile = callerProfiles.computeIfAbsent(callerId, k -> new EmotionalProfile(callerId));
        profile.updateEmotion(emotion, intensity);
        
        // Store the updated profile in memory storage for future calls
        storeCallerProfile(profile);
    }
    
    /**
     * Stores a caller's emotional profile in persistent memory
     */
    private void storeCallerProfile(EmotionalProfile profile) {
        if (memoryStorage != null) {
            try {
                String key = "emotional_profile_" + profile.getCallerId();
                String value = profile.serialize();
                memoryStorage.storeMemory(key, value, "EMOTIONAL_PROFILES");
            } catch (Exception e) {
                Log.e(TAG, "Error storing caller emotional profile", e);
            }
        }
    }
    
    /**
     * Retrieves a caller's emotional profile from persistent memory
     */
    public EmotionalProfile retrieveCallerProfile(String callerId) {
        if (callerId == null || callerId.isEmpty() || memoryStorage == null) {
            return new EmotionalProfile(callerId);
        }
        
        // Check if already in memory
        if (callerProfiles.containsKey(callerId)) {
            return callerProfiles.get(callerId);
        }
        
        // Try to retrieve from persistent storage
        try {
            String key = "emotional_profile_" + callerId;
            String value = memoryStorage.retrieveMemory(key, "EMOTIONAL_PROFILES");
            
            if (value != null && !value.isEmpty()) {
                EmotionalProfile profile = EmotionalProfile.deserialize(value);
                callerProfiles.put(callerId, profile);
                return profile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving caller emotional profile", e);
        }
        
        // If not found, create a new one
        EmotionalProfile newProfile = new EmotionalProfile(callerId);
        callerProfiles.put(callerId, newProfile);
        return newProfile;
    }
    
    /**
     * Add happy emotional tone to the response
     */
    private String addHappyEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I'm glad to hear that! " + response;
        } else if (intensity > 0.4f) {
            return "That's great. " + response;
        } else {
            return response + " I hope that helps!";
        }
    }
    
    /**
     * Add sad emotional tone to the response
     */
    private String addSadEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I'm sorry to hear that. " + response;
        } else if (intensity > 0.4f) {
            return "I understand this is difficult. " + response;
        } else {
            return response + " I hope things get better.";
        }
    }
    
    /**
     * Add angry emotional tone to the response
     */
    private String addAngryEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I understand your frustration. " + response;
        } else if (intensity > 0.4f) {
            return "Let me try to address your concern. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add afraid emotional tone to the response
     */
    private String addAfraidEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I understand you're concerned. " + response;
        } else if (intensity > 0.4f) {
            return "Let me try to address your worry. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add surprised emotional tone to the response
     */
    private String addSurprisedEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "Oh! I wasn't expecting that. " + response;
        } else if (intensity > 0.4f) {
            return "That's interesting. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add confused emotional tone to the response
     */
    private String addConfusedEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I'm trying to understand the situation. " + response;
        } else if (intensity > 0.4f) {
            return "Let me clarify. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add concerned emotional tone to the response
     */
    private String addConcernedEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "I'm concerned about this situation. " + response;
        } else if (intensity > 0.4f) {
            return "This seems important. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add curious emotional tone to the response
     */
    private String addCuriousEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "That's fascinating. " + response;
        } else if (intensity > 0.4f) {
            return "I'm curious about that. " + response;
        } else {
            return response;
        }
    }
    
    /**
     * Add reassuring emotional tone to the response
     */
    private String addReassurringEmotionalTone(String response, float intensity) {
        if (intensity > 0.7f) {
            return "Don't worry, we'll figure this out. " + response;
        } else if (intensity > 0.4f) {
            return "I'm here to help. " + response;
        } else {
            return response + " Everything will be okay.";
        }
    }
    
    /**
     * Class representing the result of emotion detection
     */
    public static class EmotionDetectionResult {
        private final EmotionState primaryEmotion;
        private final float intensity;
        
        public EmotionDetectionResult(EmotionState primaryEmotion, float intensity) {
            this.primaryEmotion = primaryEmotion;
            this.intensity = intensity;
        }
        
        public EmotionState getPrimaryEmotion() {
            return primaryEmotion;
        }
        
        public float getIntensity() {
            return intensity;
        }
    }
}
