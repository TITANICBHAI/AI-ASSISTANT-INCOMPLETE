package com.aiassistant.ai.features.voice.advanced;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.ai.features.behavior.AdaptiveBehaviorDetectionFeature;
import com.aiassistant.ai.features.behavior.BehaviorDetectionManager;
import com.aiassistant.ai.features.voice.VoiceCommandManager;
import com.aiassistant.ai.features.voice.VoiceIntegrationManager;
import com.aiassistant.ai.features.voice.VoiceResponseManager;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Advanced Voice Conversation System
 * Enhances the voice communication capabilities with:
 * - Contextual responses based on conversation history
 * - Adaptive voice characteristics based on player behavior
 * - Emotional tone variations
 * - Persona-based conversation styles
 * - Memory of past conversations
 */
public class AdvancedVoiceConversation implements 
    VoiceIntegrationManager.VoiceConversationListener {
    
    private static final String TAG = "AdvancedVoice";
    
    private final Context context;
    private final VoiceIntegrationManager voiceIntegration;
    private final VoiceResponseManager responseManager;
    private final BehaviorDetectionManager behaviorManager;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    // Voice persona
    private VoicePersona currentPersona;
    
    // Conversation state
    private ConversationState conversationState;
    
    // Conversation memory
    private final List<ConversationMemory> conversationMemory;
    
    // Response templates
    private final Map<String, List<String>> responseTemplates;
    
    // Context-specific responses
    private final Map<String, List<String>> contextResponses;
    
    // Event listeners
    private final List<AdvancedVoiceListener> listeners;
    
    // Emotional state
    private EmotionalState emotionalState;
    
    // Interruption allowance
    private boolean allowInterruptions;
    
    /**
     * Constructor
     * @param context Application context
     * @param voiceIntegration Voice integration manager
     * @param behaviorManager Behavior detection manager
     */
    public AdvancedVoiceConversation(Context context, 
                                   VoiceIntegrationManager voiceIntegration,
                                   BehaviorDetectionManager behaviorManager) {
        this.context = context;
        this.voiceIntegration = voiceIntegration;
        this.responseManager = voiceIntegration != null ? 
            ((VoiceResponseManager) voiceIntegration.getClass().getField("responseManager").get(voiceIntegration)) : null;
        this.behaviorManager = behaviorManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.currentPersona = VoicePersona.ASSISTANT;
        this.conversationState = ConversationState.IDLE;
        this.conversationMemory = new ArrayList<>();
        this.responseTemplates = new HashMap<>();
        this.contextResponses = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.emotionalState = EmotionalState.NEUTRAL;
        this.allowInterruptions = true;
        
        // Register as listener
        if (voiceIntegration != null) {
            voiceIntegration.addConversationListener(this);
        }
        
        // Initialize response templates
        initializeResponseTemplates();
        initializeContextResponses();
    }
    
    /**
     * Start an advanced voice conversation
     */
    public void startConversation() {
        if (voiceIntegration == null) {
            Log.e(TAG, "Voice integration manager is null");
            return;
        }
        
        // Update conversation state
        conversationState = ConversationState.STARTING;
        
        // Apply security context
        SecurityContext.getInstance().setCurrentFeatureActive("advanced_voice_conversation");
        
        try {
            // Start the conversation
            voiceIntegration.startConversation();
            
            // Speak greeting based on current persona and time of day
            speakGreeting();
            
            // Update state
            conversationState = ConversationState.ACTIVE;
            
            // Enable auto-response
            voiceIntegration.setAutoResponseEnabled(true);
            
            Log.d(TAG, "Advanced voice conversation started with persona: " + currentPersona);
        } catch (Exception e) {
            Log.e(TAG, "Error starting advanced voice conversation", e);
            conversationState = ConversationState.IDLE;
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Stop the conversation
     */
    public void stopConversation() {
        if (voiceIntegration == null) return;
        
        // Update state
        conversationState = ConversationState.ENDING;
        
        // Speak farewell
        speakFarewell();
        
        // Wait for farewell to complete
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // Stop the conversation
        voiceIntegration.stopConversation();
        
        // Update state
        conversationState = ConversationState.IDLE;
        
        Log.d(TAG, "Advanced voice conversation stopped");
    }
    
    /**
     * Check if conversation is active
     * @return true if active
     */
    public boolean isConversationActive() {
        return conversationState == ConversationState.ACTIVE;
    }
    
    /**
     * Set voice persona
     * @param persona Voice persona
     */
    public void setPersona(VoicePersona persona) {
        if (responseManager == null) return;
        
        this.currentPersona = persona;
        
        // Update voice characteristics based on persona
        switch (persona) {
            case ASSISTANT:
                // Standard assistant voice
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.NORMAL);
                responseManager.setSpeechPitch(1.0f);
                responseManager.setSpeechRate(1.0f);
                
                // Update characteristics
                responseManager.removeAllCharacteristics();
                responseManager.addCharacteristic("clear");
                responseManager.addCharacteristic("helpful");
                break;
                
            case TACTICAL:
                // Tactical advisor voice
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.ASSERTIVE);
                responseManager.setSpeechPitch(0.9f);
                responseManager.setSpeechRate(1.1f);
                
                // Update characteristics
                responseManager.removeAllCharacteristics();
                responseManager.addCharacteristic("precise");
                responseManager.addCharacteristic("direct");
                break;
                
            case COMPANION:
                // Friendly companion voice
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.NORMAL);
                responseManager.setSpeechPitch(1.05f);
                responseManager.setSpeechRate(1.0f);
                
                // Update characteristics
                responseManager.removeAllCharacteristics();
                responseManager.addCharacteristic("friendly");
                responseManager.addCharacteristic("supportive");
                break;
                
            case INSTRUCTOR:
                // Instructor voice
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.DETAILED);
                responseManager.setSpeechPitch(1.0f);
                responseManager.setSpeechRate(0.95f);
                
                // Update characteristics
                responseManager.removeAllCharacteristics();
                responseManager.addCharacteristic("educational");
                responseManager.addCharacteristic("patient");
                break;
                
            case STEALTH:
                // Stealth voice
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.WHISPER);
                responseManager.setSpeechPitch(1.1f);
                responseManager.setSpeechRate(0.9f);
                
                // Update characteristics
                responseManager.removeAllCharacteristics();
                responseManager.addCharacteristic("quiet");
                responseManager.addCharacteristic("secretive");
                break;
        }
        
        Log.d(TAG, "Voice persona set to: " + persona);
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onPersonaChanged(persona);
        }
    }
    
    /**
     * Get current persona
     * @return Current voice persona
     */
    public VoicePersona getCurrentPersona() {
        return currentPersona;
    }
    
    /**
     * Set emotional state
     * @param state Emotional state
     */
    public void setEmotionalState(EmotionalState state) {
        if (responseManager == null) return;
        
        this.emotionalState = state;
        
        // Adjust voice based on emotional state
        switch (state) {
            case EXCITED:
                responseManager.setSpeechPitch(responseManager.getSpeechPitch() + 0.1f);
                responseManager.setSpeechRate(responseManager.getSpeechRate() + 0.1f);
                break;
                
            case CONCERNED:
                responseManager.setSpeechPitch(responseManager.getSpeechPitch() - 0.05f);
                responseManager.setSpeechRate(responseManager.getSpeechRate() - 0.05f);
                break;
                
            case URGENT:
                responseManager.setSpeechRate(responseManager.getSpeechRate() + 0.15f);
                break;
                
            case CALM:
                responseManager.setSpeechRate(responseManager.getSpeechRate() - 0.1f);
                break;
                
            case NEUTRAL:
            default:
                // Reset to persona defaults
                setPersona(currentPersona);
                break;
        }
        
        Log.d(TAG, "Emotional state set to: " + state);
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onEmotionalStateChanged(state);
        }
    }
    
    /**
     * Get current emotional state
     * @return Current emotional state
     */
    public EmotionalState getEmotionalState() {
        return emotionalState;
    }
    
    /**
     * Set interruption allowance
     * @param allow true to allow interruptions
     */
    public void setAllowInterruptions(boolean allow) {
        this.allowInterruptions = allow;
        Log.d(TAG, "Interruptions " + (allow ? "allowed" : "disallowed"));
    }
    
    /**
     * Check if interruptions are allowed
     * @return true if allowed
     */
    public boolean isAllowingInterruptions() {
        return allowInterruptions;
    }
    
    /**
     * Speak a contextual response based on category
     * @param category Response category
     * @param replacements Values to replace in template
     */
    public void speakContextual(String category, Map<String, String> replacements) {
        if (responseManager == null) return;
        
        // Get responses for category
        List<String> responses = contextResponses.getOrDefault(category, null);
        
        if (responses == null || responses.isEmpty()) {
            Log.w(TAG, "No responses found for category: " + category);
            return;
        }
        
        // Select random response
        String template = responses.get(new Random().nextInt(responses.size()));
        
        // Apply replacements
        String finalResponse = applyReplacements(template, replacements);
        
        // Speak the response
        responseManager.speak(finalResponse);
        
        // Add to memory
        addToMemory(category, finalResponse);
    }
    
    /**
     * Speak response to user query 
     * @param query User query
     * @param response Response text
     */
    public void respondWithContext(String query, String response) {
        if (responseManager == null) return;
        
        // Check if we should adapt to player behavior
        if (behaviorManager != null && behaviorManager.isTracking()) {
            adaptToBehavior();
        }
        
        // Add context to response based on conversation history
        String enhancedResponse = enhanceWithContext(response);
        
        // Speak enhanced response
        responseManager.speak(enhancedResponse);
        
        // Add to memory
        addToMemory("user_query", query);
        addToMemory("ai_response", enhancedResponse);
    }
    
    /**
     * Add a voice listener
     * @param listener Listener to add
     */
    public void addListener(AdvancedVoiceListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a voice listener
     * @param listener Listener to remove
     */
    public void removeListener(AdvancedVoiceListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Speak a greeting based on current persona and time of day
     */
    private void speakGreeting() {
        if (responseManager == null) return;
        
        // Get greeting category based on persona
        String category = "greeting_" + currentPersona.toString().toLowerCase();
        
        // Get time of day
        String timeOfDay = getTimeOfDay();
        
        // Create replacements
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{time_of_day}", timeOfDay);
        
        // Add player style if available
        if (behaviorManager != null && behaviorManager.isTracking()) {
            String playerStyle = behaviorManager.getStrongestBehaviorTrait();
            replacements.put("{player_style}", playerStyle);
        } else {
            replacements.put("{player_style}", "player");
        }
        
        // Speak contextual greeting
        speakContextual(category, replacements);
    }
    
    /**
     * Speak a farewell based on current persona
     */
    private void speakFarewell() {
        if (responseManager == null) return;
        
        // Get farewell category based on persona
        String category = "farewell_" + currentPersona.toString().toLowerCase();
        
        // Create replacements
        Map<String, String> replacements = new HashMap<>();
        
        // Add player style if available
        if (behaviorManager != null && behaviorManager.isTracking()) {
            String playerStyle = behaviorManager.getStrongestBehaviorTrait();
            replacements.put("{player_style}", playerStyle);
        } else {
            replacements.put("{player_style}", "player");
        }
        
        // Speak contextual farewell
        speakContextual(category, replacements);
    }
    
    /**
     * Get current time of day
     * @return String representation of time of day
     */
    private String getTimeOfDay() {
        // Get current hour of day
        int hour = java.time.LocalTime.now().getHour();
        
        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 17) {
            return "afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "evening";
        } else {
            return "night";
        }
    }
    
    /**
     * Apply replacements to a template
     * @param template Template string
     * @param replacements Map of replacements
     * @return Processed string
     */
    private String applyReplacements(String template, Map<String, String> replacements) {
        String result = template;
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Add an entry to conversation memory
     * @param category Memory category
     * @param content Memory content
     */
    private void addToMemory(String category, String content) {
        ConversationMemory memory = new ConversationMemory(
            category, content, System.currentTimeMillis());
        
        conversationMemory.add(memory);
        
        // Limit memory size
        while (conversationMemory.size() > 50) {
            conversationMemory.remove(0);
        }
    }
    
    /**
     * Enhance a response with context from conversation history
     * @param response Original response
     * @return Enhanced response
     */
    private String enhanceWithContext(String response) {
        // Check if we have enough conversation history
        if (conversationMemory.size() < 3) {
            return response;
        }
        
        StringBuilder enhancedResponse = new StringBuilder(response);
        
        // Check for specific patterns in history that warrant additional context
        boolean hasAskedAboutFeatures = false;
        boolean hasExpressedUncertainty = false;
        
        // Look for patterns in last few utterances
        for (int i = Math.max(0, conversationMemory.size() - 5); i < conversationMemory.size(); i++) {
            ConversationMemory memory = conversationMemory.get(i);
            if (memory.getCategory().equals("user_query")) {
                String query = memory.getContent().toLowerCase();
                
                // Check for feature questions
                if (query.contains("what can you do") || 
                    query.contains("your features") || 
                    query.contains("your abilities")) {
                    hasAskedAboutFeatures = true;
                }
                
                // Check for uncertainty
                if (query.contains("not sure") || 
                    query.contains("don't understand") || 
                    query.contains("confused")) {
                    hasExpressedUncertainty = true;
                }
            }
        }
        
        // Add context based on patterns
        if (hasAskedAboutFeatures && !response.contains("feature") && !response.contains("ability")) {
            enhancedResponse.append(" I can also help with combat analysis, resource management, and tactical suggestions if you're interested.");
        }
        
        if (hasExpressedUncertainty && !response.contains("explain") && !response.contains("clarify")) {
            enhancedResponse.append(" Let me know if you need me to clarify anything else.");
        }
        
        return enhancedResponse.toString();
    }
    
    /**
     * Adapt voice characteristics to player behavior
     */
    private void adaptToBehavior() {
        if (behaviorManager == null || !behaviorManager.isTracking() || responseManager == null) {
            return;
        }
        
        // Get dominant behavior type
        AdaptiveBehaviorDetectionFeature.BehaviorType behaviorType = 
            behaviorManager.getDominantBehaviorType();
        
        if (behaviorType == null) {
            return;
        }
        
        // Adapt persona based on behavior type
        switch (behaviorType) {
            case AGGRESSIVE:
                setPersona(VoicePersona.TACTICAL);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.ASSERTIVE);
                break;
                
            case TACTICAL:
                setPersona(VoicePersona.TACTICAL);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.ASSERTIVE);
                break;
                
            case EXPLORER:
                setPersona(VoicePersona.COMPANION);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.NORMAL);
                break;
                
            case DEFENSIVE:
                setPersona(VoicePersona.ASSISTANT);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.NORMAL);
                break;
                
            case COLLECTOR:
                setPersona(VoicePersona.ASSISTANT);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.DETAILED);
                break;
                
            case COMPLETIONIST:
                setPersona(VoicePersona.INSTRUCTOR);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.DETAILED);
                break;
                
            case SOCIAL:
                setPersona(VoicePersona.COMPANION);
                responseManager.setSpeechMode(VoiceResponseManager.SpeechMode.NORMAL);
                break;
        }
        
        Log.d(TAG, "Adapted voice to behavior type: " + behaviorType);
    }
    
    /**
     * Initialize response templates
     */
    private void initializeResponseTemplates() {
        // Combat analysis templates
        List<String> combatTemplates = new ArrayList<>();
        combatTemplates.add("Your combat effectiveness is {effectiveness}. Consider focusing on improving your {weakness}.");
        combatTemplates.add("Analysis shows your {strength} technique is effective. {recommendation}");
        combatTemplates.add("Combat data indicates {finding}. I recommend {action}.");
        responseTemplates.put("combat_analysis", combatTemplates);
        
        // Resource management templates
        List<String> resourceTemplates = new ArrayList<>();
        resourceTemplates.add("You're using {resource_name} at {efficiency} efficiency. {recommendation}");
        resourceTemplates.add("Resource allocation can be optimized by {optimization}.");
        resourceTemplates.add("Your {resource_name} will be depleted in {time_remaining}. Consider {action}.");
        responseTemplates.put("resource_management", resourceTemplates);
        
        // Tactical suggestions templates
        List<String> tacticalTemplates = new ArrayList<>();
        tacticalTemplates.add("Tactical analysis suggests {suggestion}.");
        tacticalTemplates.add("For better positioning, try {positioning}. This will give you {advantage}.");
        tacticalTemplates.add("The optimal approach is to {approach}. This maximizes your {benefit}.");
        responseTemplates.put("tactical_suggestion", tacticalTemplates);
    }
    
    /**
     * Initialize context-specific responses
     */
    private void initializeContextResponses() {
        // Greetings for different personas
        List<String> assistantGreetings = new ArrayList<>();
        assistantGreetings.add("Good {time_of_day}. How can I assist you today?");
        assistantGreetings.add("Hello there. I'm ready to help you with your gaming needs.");
        assistantGreetings.add("Welcome back. What would you like assistance with?");
        contextResponses.put("greeting_assistant", assistantGreetings);
        
        List<String> tacticalGreetings = new ArrayList<>();
        tacticalGreetings.add("Good {time_of_day}. Tactical systems online and ready.");
        tacticalGreetings.add("Tactical advisor activated. What's the situation?");
        tacticalGreetings.add("Ready for tactical assessment. What's your objective?");
        contextResponses.put("greeting_tactical", tacticalGreetings);
        
        List<String> companionGreetings = new ArrayList<>();
        companionGreetings.add("Hey there! Great to see you again. Ready for some gaming?");
        companionGreetings.add("Good {time_of_day}, friend! What are we playing today?");
        companionGreetings.add("Hi! I'm excited to join you for another gaming session!");
        contextResponses.put("greeting_companion", companionGreetings);
        
        List<String> instructorGreetings = new ArrayList<>();
        instructorGreetings.add("Good {time_of_day}. I'm ready to help you improve your skills.");
        instructorGreetings.add("Welcome to your personal training session. What skill shall we focus on today?");
        instructorGreetings.add("I've been analyzing your previous sessions. Ready to continue your training?");
        contextResponses.put("greeting_instructor", instructorGreetings);
        
        List<String> stealthGreetings = new ArrayList<>();
        stealthGreetings.add("Stealth mode active. Secure connection established.");
        stealthGreetings.add("Communication channel secure. Voice volume reduced for discretion.");
        stealthGreetings.add("Low-profile mode engaged. How may I assist?");
        contextResponses.put("greeting_stealth", stealthGreetings);
        
        // Farewells for different personas
        List<String> assistantFarewells = new ArrayList<>();
        assistantFarewells.add("Goodbye. Contact me if you need further assistance.");
        assistantFarewells.add("Session ended. Have a good day.");
        assistantFarewells.add("Assistance mode deactivating. Until next time.");
        contextResponses.put("farewell_assistant", assistantFarewells);
        
        List<String> tacticalFarewells = new ArrayList<>();
        tacticalFarewells.add("Tactical systems standing down. Good luck out there.");
        tacticalFarewells.add("Mission support ending. Remember your training.");
        tacticalFarewells.add("Tactical advisor offline. Success in your operations.");
        contextResponses.put("farewell_tactical", tacticalFarewells);
        
        List<String> companionFarewells = new ArrayList<>();
        companionFarewells.add("Later! That was fun. Can't wait to play again soon!");
        companionFarewells.add("Bye for now! Let me know when you want to game again!");
        companionFarewells.add("Catch you later! It's always fun gaming with you!");
        contextResponses.put("farewell_companion", companionFarewells);
        
        List<String> instructorFarewells = new ArrayList<>();
        instructorFarewells.add("Session complete. Practice what you've learned.");
        instructorFarewells.add("Training concluded. I'll prepare new exercises for next time.");
        instructorFarewells.add("You've made progress today. Continue practicing until our next session.");
        contextResponses.put("farewell_instructor", instructorFarewells);
        
        List<String> stealthFarewells = new ArrayList<>();
        stealthFarewells.add("Terminating secure channel. Leaving no trace.");
        stealthFarewells.add("Stealth mode disengaging. Connection securely terminated.");
        stealthFarewells.add("Going dark. Channel closed securely.");
        contextResponses.put("farewell_stealth", stealthFarewells);
    }
    
    // VoiceConversationListener implementation
    
    @Override
    public void onVoiceSystemReady() {
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onVoiceSystemReady();
        }
    }
    
    @Override
    public void onListeningStateChanged(boolean isListening) {
        // Update state if stopped listening
        if (!isListening && conversationState == ConversationState.ACTIVE) {
            conversationState = ConversationState.IDLE;
        }
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onListeningStateChanged(isListening);
        }
    }
    
    @Override
    public void onPartialUtterance(String partialText) {
        // Check for interruption
        if (allowInterruptions && responseManager != null && 
            responseManager.isSpeaking() && partialText.length() > 5) {
            // Stop current speech to allow user to speak
            responseManager.stopSpeaking();
            
            Log.d(TAG, "Speech interrupted by user");
        }
    }
    
    @Override
    public void onUserSpeech(String utterance) {
        // Record user speech for behavior analysis
        if (behaviorManager != null && behaviorManager.isTracking()) {
            if (utterance.contains("attack") || utterance.contains("fight")) {
                behaviorManager.recordAction("communication", "aggressive", 0.8f);
            } else if (utterance.contains("help") || utterance.contains("assist")) {
                behaviorManager.recordAction("communication", "supportive", 0.7f);
            } else if (utterance.contains("explain") || utterance.contains("how to")) {
                behaviorManager.recordAction("communication", "inquisitive", 0.9f);
            }
        }
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onUserSpeech(utterance);
        }
    }
    
    @Override
    public void onAISpeaking(boolean isSpeaking) {
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onAISpeaking(isSpeaking);
        }
    }
    
    @Override
    public void onUtteranceAdded(VoiceIntegrationManager.Utterance utterance) {
        // Convert to our memory format and add to history
        if (utterance.isUserUtterance()) {
            addToMemory("user_query", utterance.getText());
        } else {
            addToMemory("ai_response", utterance.getText());
        }
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onUtteranceAdded(utterance.getText(), utterance.isUserUtterance());
        }
    }
    
    @Override
    public void onConversationCleared() {
        // Clear memory
        conversationMemory.clear();
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onConversationCleared();
        }
    }
    
    @Override
    public void onConversationError(String errorMessage) {
        // Set emotional state to concerned on error
        setEmotionalState(EmotionalState.CONCERNED);
        
        // Notify listeners
        for (AdvancedVoiceListener listener : listeners) {
            listener.onConversationError(errorMessage);
        }
    }
    
    /**
     * Voice Persona enum
     * Represents different voice personas for the AI
     */
    public enum VoicePersona {
        ASSISTANT,  // General assistant
        TACTICAL,   // Tactical advisor
        COMPANION,  // Friendly companion
        INSTRUCTOR, // Teacher/coach
        STEALTH     // Quiet, secretive
    }
    
    /**
     * Conversation State enum
     * Represents the current state of the conversation
     */
    public enum ConversationState {
        IDLE,       // Not conversing
        STARTING,   // Starting conversation
        ACTIVE,     // Actively conversing
        ENDING      // Ending conversation
    }
    
    /**
     * Emotional State enum
     * Represents the emotional tone of the AI
     */
    public enum EmotionalState {
        NEUTRAL,    // Default state
        EXCITED,    // Enthusiastic
        CONCERNED,  // Worried
        URGENT,     // High priority
        CALM        // Relaxed
    }
    
    /**
     * Conversation Memory class
     * Represents a memory of something said in conversation
     */
    private static class ConversationMemory {
        private final String category;
        private final String content;
        private final long timestamp;
        
        /**
         * Constructor
         * @param category Memory category
         * @param content Memory content
         * @param timestamp Memory timestamp
         */
        public ConversationMemory(String category, String content, long timestamp) {
            this.category = category;
            this.content = content;
            this.timestamp = timestamp;
        }
        
        /**
         * Get memory category
         * @return Memory category
         */
        public String getCategory() {
            return category;
        }
        
        /**
         * Get memory content
         * @return Memory content
         */
        public String getContent() {
            return content;
        }
        
        /**
         * Get memory timestamp
         * @return Memory timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Advanced Voice Listener interface
     * For receiving advanced voice events
     */
    public interface AdvancedVoiceListener {
        /**
         * Called when voice system is ready
         */
        void onVoiceSystemReady();
        
        /**
         * Called when listening state changes
         * @param isListening true if listening
         */
        void onListeningStateChanged(boolean isListening);
        
        /**
         * Called when user speaks
         * @param utterance User utterance
         */
        void onUserSpeech(String utterance);
        
        /**
         * Called when AI speaking state changes
         * @param isSpeaking true if speaking
         */
        void onAISpeaking(boolean isSpeaking);
        
        /**
         * Called when utterance is added
         * @param text Utterance text
         * @param isUser true if user utterance
         */
        void onUtteranceAdded(String text, boolean isUser);
        
        /**
         * Called when conversation is cleared
         */
        void onConversationCleared();
        
        /**
         * Called when there's a conversation error
         * @param errorMessage Error message
         */
        void onConversationError(String errorMessage);
        
        /**
         * Called when persona is changed
         * @param persona New persona
         */
        void onPersonaChanged(VoicePersona persona);
        
        /**
         * Called when emotional state is changed
         * @param state New emotional state
         */
        void onEmotionalStateChanged(EmotionalState state);
    }
}
