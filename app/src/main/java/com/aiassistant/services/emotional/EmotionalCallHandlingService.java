package com.aiassistant.services.emotional;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.emotional.AdvancedEmotionalIntelligence;
import com.aiassistant.core.memory.EmotionalState;
import com.aiassistant.data.models.CallerProfile;
import com.aiassistant.services.CallHandlingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that enhances call handling with emotional intelligence
 * This integrates with the CallHandlingService to provide emotionally intelligent responses
 */
public class EmotionalCallHandlingService {
    private static final String TAG = "EmotionalCallService";
    
    // Singleton instance
    private static volatile EmotionalCallHandlingService instance;
    
    // Context
    private final Context context;
    
    // Emotional intelligence engine
    private final AdvancedEmotionalIntelligence emotionalIntelligence;
    
    // AI state manager
    private final AIStateManager stateManager;
    
    // Response templates for different emotional states and response modes
    private final Map<String, Map<String, String>> responseTemplates = new HashMap<>();
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return The EmotionalCallHandlingService instance
     */
    public static synchronized EmotionalCallHandlingService getInstance(Context context) {
        if (instance == null) {
            instance = new EmotionalCallHandlingService(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private EmotionalCallHandlingService(Context context) {
        this.context = context;
        this.emotionalIntelligence = new AdvancedEmotionalIntelligence(context);
        this.stateManager = AIStateManager.getInstance(context);
        
        // Initialize response templates
        initializeResponseTemplates();
        
        Log.d(TAG, "EmotionalCallHandlingService initialized");
    }
    
    /**
     * Initialize response templates for different emotional states and modes
     */
    private void initializeResponseTemplates() {
        // Normal mode templates
        Map<String, String> normalModeTemplates = new HashMap<>();
        normalModeTemplates.put("joy", "Hi there! I'm delighted to hear from you. %s is busy right now but will call you back later.");
        normalModeTemplates.put("sadness", "Hello. I'm sorry %s can't come to the phone right now. They'll call you back when they're available.");
        normalModeTemplates.put("anger", "Hello. %s is unavailable at the moment. I'll let them know you called and they'll get back to you soon.");
        normalModeTemplates.put("fear", "Hello. %s can't speak right now, but I'll pass your message along right away.");
        normalModeTemplates.put("surprise", "Oh! Hello there! %s isn't available, but I'll definitely let them know you called!");
        normalModeTemplates.put("neutral", "Hello. %s is unavailable right now. I'll let them know you called and they'll get back to you soon.");
        responseTemplates.put("normal", normalModeTemplates);
        
        // Formal mode templates
        Map<String, String> formalModeTemplates = new HashMap<>();
        formalModeTemplates.put("joy", "Good day. I'm pleased to inform you that your call is important. %s is currently unavailable but will return your call at their earliest convenience.");
        formalModeTemplates.put("sadness", "Good day. I regret to inform you that %s is currently unavailable. They will return your call as soon as possible.");
        formalModeTemplates.put("anger", "Good day. %s is presently occupied. I will ensure they are notified of your call promptly.");
        formalModeTemplates.put("fear", "Good day. %s is unable to take your call at this time. I will notify them of your call urgently.");
        formalModeTemplates.put("surprise", "Good day. %s is currently unavailable. I will promptly inform them of your call.");
        formalModeTemplates.put("neutral", "Good day. %s is currently unavailable. I will inform them of your call, and they will contact you at their earliest convenience.");
        responseTemplates.put("formal", formalModeTemplates);
        
        // Friendly mode templates
        Map<String, String> friendlyModeTemplates = new HashMap<>();
        friendlyModeTemplates.put("joy", "Hey! Great to hear from you! %s is busy right now, but they'll be super happy to know you called!");
        friendlyModeTemplates.put("sadness", "Hey there. %s can't come to the phone right now, but they'll call you back soon, I promise!");
        friendlyModeTemplates.put("anger", "Hi there! %s is tied up at the moment, but I'll make sure they know you called right away!");
        friendlyModeTemplates.put("fear", "Hey! %s can't talk right now, but I'll tell them you called right away!");
        friendlyModeTemplates.put("surprise", "Wow! Hi there! %s isn't available, but I'll definitely let them know you reached out!");
        friendlyModeTemplates.put("neutral", "Hey there! %s can't come to the phone right now, but I'll let them know you called and they'll get back to you soon!");
        responseTemplates.put("friendly", friendlyModeTemplates);
    }
    
    /**
     * Generate an emotionally appropriate response for a call
     * @param callerProfile The caller's profile
     * @param messageFromCaller Message from the caller (if any)
     * @param userStatus Current user status
     * @param responseMode Response mode (normal, formal, friendly)
     * @return Emotionally appropriate response message
     */
    public String generateEmotionalResponse(CallerProfile callerProfile, String messageFromCaller, 
                                           String userStatus, String responseMode) {
        // Process emotional content of caller's message
        if (messageFromCaller != null && !messageFromCaller.isEmpty()) {
            emotionalIntelligence.processEmotionalContent(messageFromCaller, callerProfile);
        }
        
        // Get current emotional state
        EmotionalState state = stateManager.getEmotionalState();
        String dominantEmotion = state.dominantEmotion;
        
        // Get response templates for the current mode
        Map<String, String> templates = responseTemplates.get(responseMode);
        if (templates == null) {
            templates = responseTemplates.get("normal"); // Fallback to normal mode
        }
        
        // Get template for the current emotion
        String template = templates.get(dominantEmotion);
        if (template == null) {
            template = templates.get("neutral"); // Fallback to neutral
        }
        
        // Build user status message
        String statusMessage;
        switch (userStatus) {
            case "busy":
                statusMessage = "is very busy";
                break;
            case "meeting":
                statusMessage = "is in a meeting";
                break;
            case "driving":
                statusMessage = "is driving";
                break;
            case "sleeping":
                statusMessage = "is sleeping";
                break;
            case "unavailable":
            default:
                statusMessage = "is unavailable";
                break;
        }
        
        // Insert user status into template
        String response = String.format(template, statusMessage);
        
        // Add personalization if we know the caller well
        if (callerProfile.getCallCount() > 3 && callerProfile.getRelationshipFamiliarity() > 0.5f) {
            response = personalizeResponse(response, callerProfile);
        }
        
        // Modulate the response with the AI's current emotional state
        response = emotionalIntelligence.modulateText(response);
        
        Log.d(TAG, "Generated emotional response: " + response);
        return response;
    }
    
    /**
     * Personalize a response for a familiar caller
     * @param response Original response
     * @param callerProfile Caller profile
     * @return Personalized response
     */
    private String personalizeResponse(String response, CallerProfile callerProfile) {
        // Add personal touches based on caller relationship
        String name = callerProfile.getDisplayName();
        
        // Insert name if not already in the response
        if (!response.contains(name)) {
            response = name + ", " + response.substring(0, 1).toLowerCase() + response.substring(1);
        }
        
        // Add relationship-specific phrasing
        if (callerProfile.getRelationshipType() != null) {
            switch (callerProfile.getRelationshipType()) {
                case "family":
                    response += " Family always comes first, so they'll call you back as soon as possible.";
                    break;
                case "friend":
                    response += " You know they value your friendship, so they'll get back to you soon.";
                    break;
                case "colleague":
                    response += " They'll follow up on any work-related matters promptly.";
                    break;
            }
        }
        
        return response;
    }
    
    /**
     * Get speech parameters for text-to-speech based on emotional state
     * @return Map of speech parameters (rate, pitch, volume)
     */
    public Map<String, Float> getEmotionalSpeechParameters() {
        return emotionalIntelligence.getEmotionalSpeechParameters();
    }
    
    /**
     * Clean up resources
     */
    public void close() {
        if (emotionalIntelligence != null) {
            emotionalIntelligence.close();
        }
        instance = null;
    }
}
