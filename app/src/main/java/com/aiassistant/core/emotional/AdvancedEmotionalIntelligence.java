package com.aiassistant.core.emotional;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.neural.EmotionalIntelligenceModel;
import com.aiassistant.core.memory.EmotionalState;
import com.aiassistant.data.models.CallerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Advanced emotional intelligence engine
 * This class handles emotional analysis and response modulation
 */
public class AdvancedEmotionalIntelligence {
    private static final String TAG = "AdvancedEmotionalIntel";
    
    // Context
    private final Context context;
    
    // Emotional intelligence model
    private EmotionalIntelligenceModel emotionalModel;
    
    // AI state manager
    private AIStateManager stateManager;
    
    // Emotion intensity (0.0-1.0)
    private float emotionIntensity = 0.5f;
    
    // Emotion decay rate per second
    private float emotionDecayRate = 0.02f;
    
    // Random for subtle variations
    private Random random = new Random();
    
    /**
     * Constructor
     * @param context Application context
     */
    public AdvancedEmotionalIntelligence(Context context) {
        this.context = context;
        
        // Initialize emotional intelligence model
        emotionalModel = new EmotionalIntelligenceModel();
        boolean initSuccess = emotionalModel.initialize(context);
        Log.d(TAG, "Emotional model initialization: " + (initSuccess ? "success" : "failed"));
        
        // Get state manager
        stateManager = AIStateManager.getInstance(context);
    }
    
    /**
     * Process emotional content in a message
     * @param message Message text
     * @param callerProfile Caller profile
     */
    public void processEmotionalContent(String message, CallerProfile callerProfile) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        // Detect emotions in message
        Map<String, Float> detectedEmotions = emotionalModel.detectEmotions(message);
        
        if (detectedEmotions != null && !detectedEmotions.isEmpty()) {
            // Find dominant emotion
            String dominantEmotion = "neutral";
            float maxScore = 0;
            
            for (Map.Entry<String, Float> entry : detectedEmotions.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    dominantEmotion = entry.getKey();
                }
            }
            
            // Record emotion in caller profile
            if (callerProfile != null && !"neutral".equals(dominantEmotion)) {
                callerProfile.recordEmotion(dominantEmotion);
            }
            
            // Update AI emotional state based on caller's emotion
            updateEmotionalState(dominantEmotion, maxScore);
            
            Log.d(TAG, "Detected emotion: " + dominantEmotion + " with score: " + maxScore);
        }
    }
    
    /**
     * Update the AI's emotional state based on caller's emotion
     * @param callerEmotion Detected caller emotion
     * @param intensity Emotion intensity (0.0-1.0)
     */
    private void updateEmotionalState(String callerEmotion, float intensity) {
        if (stateManager == null) {
            return;
        }
        
        // Get current emotional state
        EmotionalState currentState = stateManager.getEmotionalState();
        
        // Determine response emotional state based on caller's emotion
        String responseEmotion;
        
        switch (callerEmotion) {
            case "joy":
                // Respond with joy (mirroring positive emotion)
                responseEmotion = "joy";
                break;
                
            case "sadness":
                // Respond with empathy
                responseEmotion = "sadness";
                // Lower intensity to show balanced empathy
                intensity *= 0.7f;
                break;
                
            case "anger":
                // Respond with calm to de-escalate
                responseEmotion = "neutral";
                // Add a small amount of concern
                stateManager.adjustEmotionLevel("concern", 0.3f);
                break;
                
            case "fear":
                // Respond with reassurance
                responseEmotion = "joy";
                // Lower intensity to be comforting but not dismissive
                intensity *= 0.5f;
                // Add a small amount of concern
                stateManager.adjustEmotionLevel("concern", 0.4f);
                break;
                
            case "surprise":
                // Mirror surprise but with less intensity
                responseEmotion = "surprise";
                intensity *= 0.8f;
                break;
                
            case "disgust":
                // Respond with neutral to avoid escalation
                responseEmotion = "neutral";
                break;
                
            case "neutral":
            default:
                // Match neutral with neutral
                responseEmotion = "neutral";
                break;
        }
        
        // Update emotional state with appropriate intensity
        stateManager.setEmotionLevel(responseEmotion, intensity);
        
        Log.d(TAG, "Updated emotional state to: " + responseEmotion + " with intensity: " + intensity);
    }
    
    /**
     * Modulate text based on current emotional state
     * @param text Original text
     * @return Emotionally modulated text
     */
    public String modulateText(String text) {
        if (stateManager == null) {
            return text;
        }
        
        // Get current emotional state
        EmotionalState state = stateManager.getEmotionalState();
        String dominantEmotion = state.dominantEmotion;
        float dominantIntensity = state.dominantIntensity;
        
        // Only modulate if intensity is significant
        if (dominantIntensity < 0.3f || "neutral".equals(dominantEmotion)) {
            return text;
        }
        
        // Create emotion-specific modulations
        switch (dominantEmotion) {
            case "joy":
                return modulateJoy(text, dominantIntensity);
                
            case "sadness":
                return modulateSadness(text, dominantIntensity);
                
            case "anger":
                return modulateAnger(text, dominantIntensity);
                
            case "fear":
                return modulateFear(text, dominantIntensity);
                
            case "surprise":
                return modulateSurprise(text, dominantIntensity);
                
            default:
                return text;
        }
    }
    
    /**
     * Modulate text for joy emotion
     */
    private String modulateJoy(String text, float intensity) {
        if (intensity > 0.7f) {
            // High joy
            return text.replaceAll("\\.$", "!") + " I'm happy to help!";
        } else if (intensity > 0.5f) {
            // Medium joy
            return text + " I'm glad I could assist.";
        } else {
            // Low joy
            return text.replaceAll("\\.$", " :).");
        }
    }
    
    /**
     * Modulate text for sadness emotion
     */
    private String modulateSadness(String text, float intensity) {
        if (intensity > 0.7f) {
            // High sadness
            return "I understand this is difficult... " + text;
        } else if (intensity > 0.5f) {
            // Medium sadness
            return text + " I hope things get better soon.";
        } else {
            // Low sadness
            return text;
        }
    }
    
    /**
     * Modulate text for anger emotion
     */
    private String modulateAnger(String text, float intensity) {
        if (intensity > 0.7f) {
            // High anger - should rarely happen for AI
            return text.toUpperCase();
        } else if (intensity > 0.5f) {
            // Medium anger
            return text.replaceAll("\\.$", ".");
        } else {
            // Low anger
            return text;
        }
    }
    
    /**
     * Modulate text for fear emotion
     */
    private String modulateFear(String text, float intensity) {
        if (intensity > 0.7f) {
            // High fear
            return text + " Please let me know if you need urgent assistance.";
        } else if (intensity > 0.5f) {
            // Medium fear
            return text + " I'll make sure your message is delivered promptly.";
        } else {
            // Low fear
            return text;
        }
    }
    
    /**
     * Modulate text for surprise emotion
     */
    private String modulateSurprise(String text, float intensity) {
        if (intensity > 0.7f) {
            // High surprise
            return "Oh! " + text.replaceAll("\\.$", "!");
        } else if (intensity > 0.5f) {
            // Medium surprise
            return "Wow! " + text;
        } else {
            // Low surprise
            return text;
        }
    }
    
    /**
     * Get speech parameters based on emotional state
     * @return Map of speech parameters (rate, pitch, volume)
     */
    public Map<String, Float> getEmotionalSpeechParameters() {
        if (stateManager == null) {
            return getDefaultSpeechParameters();
        }
        
        // Get current emotional state
        EmotionalState state = stateManager.getEmotionalState();
        String dominantEmotion = state.dominantEmotion;
        float dominantIntensity = state.dominantIntensity;
        
        // Create base parameters
        Map<String, Float> params = new HashMap<>();
        
        // Set default parameters
        params.put("speechRate", 1.0f);
        params.put("pitch", 1.0f);
        params.put("volume", 1.0f);
        
        // Only modulate if intensity is significant
        if (dominantIntensity < 0.3f || "neutral".equals(dominantEmotion)) {
            return params;
        }
        
        // Apply small random variation (±5%)
        float randomFactor = 0.95f + (random.nextFloat() * 0.1f);
        
        // Create emotion-specific modulations
        switch (dominantEmotion) {
            case "joy":
                // Joy: slightly faster, higher pitch
                params.put("speechRate", 1.1f * randomFactor);
                params.put("pitch", 1.1f * randomFactor);
                break;
                
            case "sadness":
                // Sadness: slower, lower pitch
                params.put("speechRate", 0.9f * randomFactor);
                params.put("pitch", 0.9f * randomFactor);
                break;
                
            case "anger":
                // Anger: faster, higher volume
                params.put("speechRate", 1.15f * randomFactor);
                params.put("volume", 1.1f * randomFactor);
                break;
                
            case "fear":
                // Fear: faster, higher pitch
                params.put("speechRate", 1.1f * randomFactor);
                params.put("pitch", 1.15f * randomFactor);
                break;
                
            case "surprise":
                // Surprise: higher pitch
                params.put("pitch", 1.2f * randomFactor);
                break;
        }
        
        // Scale parameter changes by intensity
        for (Map.Entry<String, Float> entry : new HashMap<>(params).entrySet()) {
            if (!entry.getKey().equals("volume")) { // Don't scale volume too much
                float baseValue = 1.0f;
                float delta = entry.getValue() - baseValue;
                params.put(entry.getKey(), baseValue + (delta * dominantIntensity));
            }
        }
        
        return params;
    }
    
    /**
     * Get default speech parameters
     * @return Map of default speech parameters
     */
    private Map<String, Float> getDefaultSpeechParameters() {
        Map<String, Float> params = new HashMap<>();
        params.put("speechRate", 1.0f);
        params.put("pitch", 1.0f);
        params.put("volume", 1.0f);
        return params;
    }
    
    /**
     * Clean up resources
     */
    public void close() {
        if (emotionalModel != null) {
            emotionalModel.close();
        }
    }
}
