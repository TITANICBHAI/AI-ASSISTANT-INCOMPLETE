package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Voice Integration Manager
 * Combines voice command and voice response features
 * for two-way voice communication
 */
public class VoiceIntegrationManager implements 
    VoiceCommandManager.VoiceCommandEventListener,
    VoiceResponseManager.VoiceOutputListener {
    
    private static final String TAG = "VoiceIntegration";
    
    private final Context context;
    private final VoiceCommandManager commandManager;
    private final VoiceResponseManager responseManager;
    private final List<VoiceConversationListener> conversationListeners;
    
    // Auto-response mode
    private boolean autoResponseEnabled;
    
    // Conversation history
    private final List<Utterance> conversationHistory;
    
    /**
     * Constructor
     * @param context Application context
     * @param commandManager Voice command manager
     * @param responseManager Voice response manager
     */
    public VoiceIntegrationManager(Context context, 
                                 VoiceCommandManager commandManager,
                                 VoiceResponseManager responseManager) {
        this.context = context;
        this.commandManager = commandManager;
        this.responseManager = responseManager;
        this.conversationListeners = new ArrayList<>();
        this.autoResponseEnabled = false;
        this.conversationHistory = new ArrayList<>();
        
        // Register as listeners
        commandManager.addEventListener(this);
        responseManager.addOutputListener(this);
    }
    
    /**
     * Start voice conversation
     */
    public void startConversation() {
        // Start listening for commands
        commandManager.startListening();
        
        // Speak greeting
        responseManager.speak("Hello, how can I assist you?");
        
        Log.d(TAG, "Voice conversation started");
    }
    
    /**
     * Stop voice conversation
     */
    public void stopConversation() {
        // Stop listening for commands
        commandManager.stopListening();
        
        // Stop any ongoing speech
        responseManager.stopSpeaking();
        
        Log.d(TAG, "Voice conversation stopped");
    }
    
    /**
     * Set auto-response mode
     * @param enabled true to enable auto-responses
     */
    public void setAutoResponseEnabled(boolean enabled) {
        this.autoResponseEnabled = enabled;
        Log.d(TAG, "Auto-response " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if auto-response is enabled
     * @return true if enabled
     */
    public boolean isAutoResponseEnabled() {
        return autoResponseEnabled;
    }
    
    /**
     * Process user query and generate response
     * @param query User query
     * @return AI response
     */
    public String processQuery(String query) {
        // In a real implementation, this would generate responses
        // For this demo, return a simple acknowledgment
        
        // Add to conversation history
        addToConversation(query, true);
        
        // Generate response
        String response = generateBasicResponse(query);
        
        // Add to conversation history
        addToConversation(response, false);
        
        return response;
    }
    
    /**
     * Speak a response to a query
     * @param query User query
     */
    public void respondToQuery(String query) {
        String response = processQuery(query);
        
        // Speak the response
        responseManager.speak(response);
    }
    
    /**
     * Get conversation history
     * @return List of utterances
     */
    public List<Utterance> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Clear conversation history
     */
    public void clearConversationHistory() {
        conversationHistory.clear();
        
        // Notify listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onConversationCleared();
        }
    }
    
    /**
     * Add a voice conversation listener
     * @param listener Listener to add
     */
    public void addConversationListener(VoiceConversationListener listener) {
        if (listener != null && !conversationListeners.contains(listener)) {
            conversationListeners.add(listener);
        }
    }
    
    /**
     * Remove a voice conversation listener
     * @param listener Listener to remove
     */
    public void removeConversationListener(VoiceConversationListener listener) {
        conversationListeners.remove(listener);
    }
    
    /**
     * Add an utterance to the conversation
     * @param text Utterance text
     * @param isUser true if user utterance, false if AI
     */
    private void addToConversation(String text, boolean isUser) {
        Utterance utterance = new Utterance(text, isUser, System.currentTimeMillis());
        conversationHistory.add(utterance);
        
        // Notify listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onUtteranceAdded(utterance);
        }
    }
    
    /**
     * Generate a basic response
     * @param query User query
     * @return AI response
     */
    private String generateBasicResponse(String query) {
        // Simple response generation for demo purposes
        
        query = query.toLowerCase();
        
        if (query.contains("hello") || query.contains("hi ")) {
            return "Hello! How can I help you?";
        } else if (query.contains("how are you")) {
            return "I'm functioning well, thank you for asking.";
        } else if (query.contains("help")) {
            return "I can help you with game analysis, resource management, and tactical advice. What do you need?";
        } else if (query.contains("thank")) {
            return "You're welcome!";
        } else if (query.contains("bye") || query.contains("goodbye")) {
            return "Goodbye! Let me know if you need assistance later.";
        } else if (query.contains("feature") || query.contains("what can you do")) {
            return "I offer pattern recognition, combat analysis, environmental analysis, resource management, and more.";
        } else {
            return "I understand your request. How else can I assist you?";
        }
    }
    
    // VoiceCommandEventListener implementation
    
    @Override
    public void onListeningEvent(VoiceCommandManager.ListeningEvent event) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onListeningStateChanged(event == VoiceCommandManager.ListeningEvent.STARTED);
        }
    }
    
    @Override
    public void onVolumeChanged(float volume) {
        // Not needed for conversation
    }
    
    @Override
    public void onPartialResult(String partialResult) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onPartialUtterance(partialResult);
        }
    }
    
    @Override
    public void onCommandReceived(String command) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onUserSpeech(command);
        }
        
        // If command processing fails, handle as general query
        boolean processed = commandManager.processCommand(command);
        
        // If auto-response is enabled and command wasn't processed as a specific command
        if (autoResponseEnabled && !processed) {
            respondToQuery(command);
        }
    }
    
    @Override
    public void onCommandProcessed(String command) {
        // Command was processed by the command system
        // No need for auto-response
    }
    
    @Override
    public void onCommandNotRecognized(String command) {
        // If auto-response is enabled
        if (autoResponseEnabled) {
            respondToQuery(command);
        }
    }
    
    @Override
    public void onError(String error) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onConversationError("Command error: " + error);
        }
    }
    
    // VoiceOutputListener implementation
    
    @Override
    public void onVoiceReady() {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onVoiceSystemReady();
        }
    }
    
    @Override
    public void onTextQueued(String text) {
        // Not needed for conversation
    }
    
    @Override
    public void onSpeakingStarted(String text) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onAISpeaking(true);
        }
    }
    
    @Override
    public void onSpeakingFinished() {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onAISpeaking(false);
        }
        
        // Resume listening for commands if conversation is ongoing
        if (commandManager.isListening()) {
            commandManager.startListening();
        }
    }
    
    @Override
    public void onSpeakingStopped() {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onAISpeaking(false);
        }
    }
    
    @Override
    public void onVoiceError(String errorMessage) {
        // Notify conversation listeners
        for (VoiceConversationListener listener : conversationListeners) {
            listener.onConversationError("Voice error: " + errorMessage);
        }
    }
    
    /**
     * Utterance class
     * Represents a single utterance in a conversation
     */
    public static class Utterance {
        private final String text;
        private final boolean isUser;
        private final long timestamp;
        
        /**
         * Constructor
         * @param text Utterance text
         * @param isUser true if user utterance, false if AI
         * @param timestamp Utterance timestamp
         */
        public Utterance(String text, boolean isUser, long timestamp) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }
        
        /**
         * Get utterance text
         * @return Utterance text
         */
        public String getText() {
            return text;
        }
        
        /**
         * Check if user utterance
         * @return true if user utterance, false if AI
         */
        public boolean isUserUtterance() {
            return isUser;
        }
        
        /**
         * Get utterance timestamp
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Voice Conversation Listener interface
     * For receiving voice conversation events
     */
    public interface VoiceConversationListener {
        /**
         * Called when voice system is ready
         */
        void onVoiceSystemReady();
        
        /**
         * Called when listening state changes
         * @param isListening true if listening, false otherwise
         */
        void onListeningStateChanged(boolean isListening);
        
        /**
         * Called with partial utterance
         * @param partialText Partial utterance text
         */
        void onPartialUtterance(String partialText);
        
        /**
         * Called when user speaks
         * @param utterance User utterance
         */
        void onUserSpeech(String utterance);
        
        /**
         * Called when AI speaking state changes
         * @param isSpeaking true if speaking, false otherwise
         */
        void onAISpeaking(boolean isSpeaking);
        
        /**
         * Called when utterance is added to conversation
         * @param utterance Added utterance
         */
        void onUtteranceAdded(Utterance utterance);
        
        /**
         * Called when conversation is cleared
         */
        void onConversationCleared();
        
        /**
         * Called when there's a conversation error
         * @param errorMessage Error message
         */
        void onConversationError(String errorMessage);
    }
}
