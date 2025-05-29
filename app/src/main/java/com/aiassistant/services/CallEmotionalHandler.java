package com.aiassistant.services;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.emotional.EmotionState;
import com.aiassistant.core.emotional.EmotionalIntelligenceManager;
import com.aiassistant.core.emotional.EmotionalProfile;
import com.aiassistant.core.speech.SpeechSynthesisManager;
import com.aiassistant.learning.memory.MemoryStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the emotional aspects of phone calls, integrating the emotional intelligence
 * system with the call handling service. This class processes caller speech, detects
 * emotions, and adjusts responses based on emotional context.
 */
public class CallEmotionalHandler {
    private static final String TAG = "CallEmotionalHandler";
    
    // The context for accessing Android services
    private final Context context;
    
    // Dependencies
    private final EmotionalIntelligenceManager emotionalManager;
    private final SpeechSynthesisManager speechManager;
    private final AIStateManager aiStateManager;
    private final MemoryStorage memoryStorage;
    
    // Map to store active call emotional contexts
    private final Map<String, CallEmotionalContext> activeCallContexts = new HashMap<>();
    
    // Settings
    private boolean applyEmotionalTone = true;
    private boolean storeEmotionalProfiles = true;
    private boolean adaptToCallerEmotions = true;
    
    /**
     * Constructor
     */
    public CallEmotionalHandler(Context context, EmotionalIntelligenceManager emotionalManager,
                               SpeechSynthesisManager speechManager, AIStateManager aiStateManager,
                               MemoryStorage memoryStorage) {
        this.context = context;
        this.emotionalManager = emotionalManager;
        this.speechManager = speechManager;
        this.aiStateManager = aiStateManager;
        this.memoryStorage = memoryStorage;
    }
    
    /**
     * Starts emotional handling for a new call and returns a unique ID for tracking this call
     */
    public String handleCallStart(String callerId) {
        // Generate a unique ID for this call
        String emotionalCallId = UUID.randomUUID().toString();
        
        // Check if emotional intelligence is enabled
        if (!emotionalManager.isEnabled()) {
            Log.d(TAG, "Emotional intelligence is disabled, minimal emotional handling for call");
            CallEmotionalContext context = new CallEmotionalContext(callerId, false);
            activeCallContexts.put(emotionalCallId, context);
            return emotionalCallId;
        }
        
        // Retrieve or create the emotional profile for this caller
        EmotionalProfile callerProfile = emotionalManager.retrieveCallerProfile(callerId);
        Log.d(TAG, "Retrieved emotional profile for caller: " + callerId);
        
        // Record the start of this emotional interaction
        callerProfile.recordInteractionStart();
        
        // Determine if this call might need special emotional handling
        boolean needsSpecialHandling = determineIfCallNeedsEmotionalHandling(callerProfile);
        
        // Create and store the context
        CallEmotionalContext context = new CallEmotionalContext(callerId, needsSpecialHandling);
        context.setCallerProfile(callerProfile);
        activeCallContexts.put(emotionalCallId, context);
        
        Log.d(TAG, "Started emotional handling for call: " + emotionalCallId + 
              ", caller: " + callerId + ", needs special handling: " + needsSpecialHandling);
        
        return emotionalCallId;
    }
    
    /**
     * Initialize handling for a new call (legacy method for backward compatibility)
     */
    public void initializeCall(String callerId, String callerName) {
        String emotionalCallId = handleCallStart(callerId);
        Log.d(TAG, "Initialized call with legacy method, new emotional call ID: " + emotionalCallId);
    }
    
    /**
     * Analyze call metadata to see if this call might need emotional handling
     */
    private boolean determineIfCallNeedsEmotionalHandling(EmotionalProfile profile) {
        // If profile shows caller has negative bias or strong emotions, prepare for emotional handling
        if (profile.hasNegativeBias() || profile.hasStrongEmotions()) {
            Log.d(TAG, "Detected caller with emotional history, preparing for sensitive handling");
            return true;
        }
        
        // If the most frequent emotion is a negative one
        EmotionState mostCommon = profile.getMostCommonEmotion();
        if (mostCommon.isNegative() && profile.getEmotionFrequency(mostCommon) >= 3) {
            Log.d(TAG, "Caller frequently expresses " + mostCommon + ", preparing for sensitive handling");
            return true;
        }
        
        return false;
    }
    
    /**
     * Process caller speech to detect emotions
     */
    public void processCallerSpeech(String emotionalCallId, String speech) {
        if (!emotionalManager.isEnabled() || speech == null || speech.isEmpty()) {
            return;
        }
        
        CallEmotionalContext context = activeCallContexts.get(emotionalCallId);
        if (context == null) {
            Log.w(TAG, "Attempt to process speech for unknown call ID: " + emotionalCallId);
            return;
        }
        
        // Detect emotions in the speech
        EmotionalIntelligenceManager.EmotionDetectionResult result = 
            emotionalManager.detectEmotion(speech, context.getCallerId());
        
        EmotionState detectedEmotion = result.getPrimaryEmotion();
        float intensity = result.getIntensity();
        
        Log.d(TAG, "Detected emotion in caller speech: " + detectedEmotion + 
              " with intensity " + intensity);
        
        // Store detected emotion in the context
        context.setLastDetectedEmotion(detectedEmotion, intensity);
        
        // If this is a strong emotion, adjust the AI state manager
        if (intensity > 0.7f) {
            adjustAIEmotionalState(detectedEmotion, intensity);
        }
    }
    
    /**
     * Adjust the AI's emotional state based on detected caller emotions
     */
    private void adjustAIEmotionalState(EmotionState callerEmotion, float intensity) {
        if (!adaptToCallerEmotions) {
            return;
        }
        
        // Determine an appropriate AI emotional response
        EmotionState aiResponse = determineAppropriateEmotionalResponse(callerEmotion);
        
        // Calculate response intensity (usually lower than caller intensity)
        float responseIntensity = Math.min(intensity * 0.8f, 0.9f);
        
        // Update the AI state manager
        emotionalManager.setCurrentEmotionalState(aiResponse, responseIntensity);
        Log.d(TAG, "AI emotional state adjusted to " + aiResponse + 
              " with intensity " + responseIntensity + " in response to caller");
    }
    
    /**
     * Determine an appropriate emotional response to the caller's emotion
     */
    private EmotionState determineAppropriateEmotionalResponse(EmotionState callerEmotion) {
        // Map caller emotions to appropriate AI responses
        switch (callerEmotion) {
            case ANGRY:
            case FRUSTRATED:
                return EmotionState.CALM;
                
            case SAD:
            case DISAPPOINTED:
                return EmotionState.COMPASSIONATE;
                
            case HAPPY:
            case EXCITED:
                return EmotionState.HAPPY;
                
            case AFRAID:
            case ANXIOUS:
                return EmotionState.REASSURING;
                
            case CONFUSED:
                return EmotionState.HELPFUL;
                
            default:
                return EmotionState.NEUTRAL;
        }
    }
    
    /**
     * Generate an emotionally-aware response based on the original response
     */
    public String generateEmotionalResponse(String emotionalCallId, String originalResponse) {
        return applyEmotionalAdjustments(emotionalCallId, originalResponse);
    }
    
    /**
     * Apply emotional adjustments to the original response
     */
    public String applyEmotionalAdjustments(String emotionalCallId, String originalResponse) {
        if (!emotionalManager.isEnabled() || !applyEmotionalTone || originalResponse == null) {
            return originalResponse;
        }
        
        CallEmotionalContext context = activeCallContexts.get(emotionalCallId);
        if (context == null) {
            Log.w(TAG, "Attempt to adjust response for unknown call ID: " + emotionalCallId);
            return originalResponse;
        }
        
        // Get the current AI emotional state
        EmotionState aiEmotion = emotionalManager.getCurrentEmotionalState();
        
        // Get the caller's last detected emotion
        EmotionState callerEmotion = context.getLastDetectedEmotion();
        float callerIntensity = context.getLastEmotionalIntensity();
        
        // Apply emotional adjustments
        String adjustedResponse = originalResponse;
        
        if (context.needsSpecialHandling()) {
            adjustedResponse = generateSpecialEmotionalResponse(
                originalResponse, callerEmotion, callerIntensity, aiEmotion);
        } else {
            adjustedResponse = generateStandardEmotionalResponse(
                originalResponse, callerEmotion, callerIntensity, aiEmotion);
        }
        
        Log.d(TAG, "Applied emotional adjustments to response for call: " + emotionalCallId);
        return adjustedResponse;
    }
    
    /**
     * Apply speech synthesis parameters based on emotional state
     */
    public void applyEmotionalSpeechParameters(String emotionalCallId) {
        if (!emotionalManager.isEnabled() || speechManager == null) {
            return;
        }
        
        CallEmotionalContext context = activeCallContexts.get(emotionalCallId);
        if (context == null) {
            Log.w(TAG, "Attempt to adjust speech for unknown call ID: " + emotionalCallId);
            return;
        }
        
        // Get the current AI emotional state
        EmotionState aiEmotion = emotionalManager.getCurrentEmotionalState();
        
        // Adjust speech parameters based on emotion
        switch (aiEmotion) {
            case HAPPY:
                speechManager.adjustParameters(1.1f, 1.1f); // Slightly faster, higher pitch
                break;
            case SAD:
                speechManager.adjustParameters(0.9f, 0.9f); // Slower, lower pitch
                break;
            case ANGRY:
                speechManager.adjustParameters(1.2f, 1.0f); // Faster, normal pitch
                break;
            case AFRAID:
                speechManager.adjustParameters(1.1f, 1.2f); // Slightly faster, higher pitch
                break;
            case CONCERNED:
                speechManager.adjustParameters(0.95f, 1.0f); // Slightly slower, normal pitch
                break;
            case REASSURING:
                speechManager.adjustParameters(0.9f, 1.05f); // Slower, slightly higher pitch
                break;
            default:
                speechManager.resetParameters(); // Default parameters
                break;
        }
        
        Log.d(TAG, "Applied emotional speech parameters for " + aiEmotion);
    }
    
    /**
     * Generate a standard emotional response
     */
    private String generateStandardEmotionalResponse(String original, EmotionState callerEmotion, 
                                                   float callerIntensity, EmotionState aiEmotion) {
        // For standard responses, add subtle emotional cues
        if (aiEmotion == EmotionState.NEUTRAL) {
            return original;
        }
        
        // Add emotion indicators based on AI's emotional state
        String emotionalPrefix = "";
        String emotionalSuffix = "";
        
        switch (aiEmotion) {
            case HAPPY:
                if (Math.random() < 0.3) {
                    emotionalPrefix = "I'm glad to help! ";
                }
                break;
                
            case COMPASSIONATE:
                if (Math.random() < 0.4) {
                    emotionalPrefix = "I understand. ";
                }
                break;
                
            case REASSURING:
                if (Math.random() < 0.5) {
                    emotionalPrefix = "Don't worry. ";
                }
                break;
                
            case HELPFUL:
                if (Math.random() < 0.3) {
                    emotionalPrefix = "Let me assist you. ";
                }
                break;
                
            case CALM:
                if (Math.random() < 0.4) {
                    emotionalPrefix = "I hear your concern. ";
                }
                break;
        }
        
        return emotionalPrefix + original + emotionalSuffix;
    }
    
    /**
     * Generate a special emotional response for sensitive situations
     */
    private String generateSpecialEmotionalResponse(String original, EmotionState callerEmotion,
                                                  float callerIntensity, EmotionState aiEmotion) {
        // For callers with emotional patterns, provide more tailored responses
        String response = original;
        
        // Special handling for negative emotions with high intensity
        if (callerEmotion.isNegative() && callerIntensity > 0.7f) {
            switch (callerEmotion) {
                case ANGRY:
                case FRUSTRATED:
                    response = "I understand this is frustrating. " + response;
                    break;
                    
                case SAD:
                case DISAPPOINTED:
                    response = "I'm sorry to hear that. " + response;
                    break;
                    
                case AFRAID:
                case ANXIOUS:
                    response = "It's okay, I'm here to help. " + response;
                    break;
            }
        }
        
        return response;
    }
    
    /**
     * Handle the end of a call
     */
    public void handleCallEnd(String emotionalCallId) {
        CallEmotionalContext context = activeCallContexts.get(emotionalCallId);
        if (context == null) {
            Log.w(TAG, "Attempt to handle end for unknown call ID: " + emotionalCallId);
            return;
        }
        
        if (emotionalManager.isEnabled() && storeEmotionalProfiles) {
            try {
                // Save the emotional profile
                String callerId = context.getCallerId();
                EmotionalProfile profile = context.getCallerProfile();
                
                // Record the end of this emotional interaction
                profile.recordInteractionEnd();
                
                // Save the updated profile
                emotionalManager.saveCallerProfile(callerId, profile);
                Log.d(TAG, "Saved emotional profile for caller: " + callerId);
            } catch (Exception e) {
                Log.e(TAG, "Error saving emotional profile", e);
            }
        }
        
        // Remove the context from our active calls
        activeCallContexts.remove(emotionalCallId);
        Log.d(TAG, "Ended emotional handling for call: " + emotionalCallId);
    }
    
    /**
     * Inner class to maintain the emotional context for a single call
     */
    private class CallEmotionalContext {
        private final String callerId;
        private boolean needsSpecialHandling;
        private EmotionState lastDetectedEmotion = EmotionState.NEUTRAL;
        private float lastEmotionalIntensity = 0.0f;
        private EmotionalProfile callerProfile;
        
        public CallEmotionalContext(String callerId, boolean needsSpecialHandling) {
            this.callerId = callerId;
            this.needsSpecialHandling = needsSpecialHandling;
        }
        
        public String getCallerId() {
            return callerId;
        }
        
        public boolean needsSpecialHandling() {
            return needsSpecialHandling;
        }
        
        public void setNeedsSpecialHandling(boolean needsSpecialHandling) {
            this.needsSpecialHandling = needsSpecialHandling;
        }
        
        public EmotionState getLastDetectedEmotion() {
            return lastDetectedEmotion;
        }
        
        public float getLastEmotionalIntensity() {
            return lastEmotionalIntensity;
        }
        
        public void setLastDetectedEmotion(EmotionState emotion, float intensity) {
            this.lastDetectedEmotion = emotion;
            this.lastEmotionalIntensity = intensity;
            
            // Update the caller's emotional profile
            if (callerProfile != null) {
                callerProfile.recordEmotion(emotion, intensity);
            }
        }
        
        public EmotionalProfile getCallerProfile() {
            return callerProfile;
        }
        
        public void setCallerProfile(EmotionalProfile profile) {
            this.callerProfile = profile;
        }
    }
}
