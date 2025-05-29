package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.FeatureManager;
import com.aiassistant.ai.features.AIFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Voice Command Manager
 * Simplified interface for using the voice command feature
 */
public class VoiceCommandManager implements VoiceCommandFeature.VoiceCommandListener {
    private static final String TAG = "VoiceCommandManager";
    
    private final Context context;
    private final VoiceCommandFeature voiceCommandFeature;
    private final FeatureManager featureManager;
    private final List<VoiceCommandEventListener> eventListeners;
    private final List<String> commandHistory;
    private static final int MAX_HISTORY = 20;
    
    // Command categories
    private final Map<String, List<String>> commandCategories;
    
    /**
     * Constructor
     * @param context Application context
     * @param voiceCommandFeature Voice command feature
     * @param featureManager Feature manager
     */
    public VoiceCommandManager(Context context, VoiceCommandFeature voiceCommandFeature,
                             FeatureManager featureManager) {
        this.context = context;
        this.voiceCommandFeature = voiceCommandFeature;
        this.featureManager = featureManager;
        this.eventListeners = new ArrayList<>();
        this.commandHistory = new ArrayList<>();
        this.commandCategories = new HashMap<>();
        
        // Register as listener
        voiceCommandFeature.addCommandListener(this);
        
        // Set up command categories
        setupCommandCategories();
        
        // Register feature control commands
        registerFeatureControlCommands();
    }
    
    /**
     * Start listening for voice commands
     */
    public void startListening() {
        if (voiceCommandFeature.isEnabled() && !voiceCommandFeature.isListening()) {
            voiceCommandFeature.startListening();
        }
    }
    
    /**
     * Stop listening for voice commands
     */
    public void stopListening() {
        if (voiceCommandFeature.isEnabled() && voiceCommandFeature.isListening()) {
            voiceCommandFeature.stopListening();
        }
    }
    
    /**
     * Check if listening
     * @return true if listening
     */
    public boolean isListening() {
        return voiceCommandFeature.isEnabled() && voiceCommandFeature.isListening();
    }
    
    /**
     * Set recognition language
     * @param languageCode Language code
     */
    public void setLanguage(String languageCode) {
        if (voiceCommandFeature.isEnabled()) {
            voiceCommandFeature.setLanguage(languageCode);
        }
    }
    
    /**
     * Get current language
     * @return Language code
     */
    public String getLanguage() {
        return voiceCommandFeature.getLanguage();
    }
    
    /**
     * Process a text command
     * @param commandText Command text
     * @return true if command was processed
     */
    public boolean processCommand(String commandText) {
        if (voiceCommandFeature.isEnabled()) {
            return voiceCommandFeature.processTextCommand(commandText);
        }
        return false;
    }
    
    /**
     * Add a custom command
     * @param phrase Custom phrase
     * @param command Actual command
     */
    public void addCustomCommand(String phrase, String command) {
        if (voiceCommandFeature.isEnabled()) {
            voiceCommandFeature.addCustomCommand(phrase, command);
        }
    }
    
    /**
     * Register a custom command handler
     * @param pattern Command pattern
     * @param category Command category
     * @param handler Command handler
     */
    public void registerCommand(String pattern, String category, 
                              VoiceCommandFeature.CommandHandler handler) {
        if (voiceCommandFeature.isEnabled()) {
            voiceCommandFeature.registerCommand(pattern, handler);
            
            // Add to category
            if (category != null && !category.isEmpty()) {
                List<String> commands = commandCategories.computeIfAbsent(
                    category, k -> new ArrayList<>());
                commands.add(pattern);
            }
        }
    }
    
    /**
     * Get command history
     * @return List of recent commands
     */
    public List<String> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }
    
    /**
     * Get commands by category
     * @param category Category name
     * @return List of commands in category
     */
    public List<String> getCommandsByCategory(String category) {
        return new ArrayList<>(commandCategories.getOrDefault(
            category, new ArrayList<>()));
    }
    
    /**
     * Get all command categories
     * @return List of category names
     */
    public List<String> getCommandCategories() {
        return new ArrayList<>(commandCategories.keySet());
    }
    
    /**
     * Add an event listener
     * @param listener Event listener
     */
    public void addEventListener(VoiceCommandEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Remove an event listener
     * @param listener Event listener
     */
    public void removeEventListener(VoiceCommandEventListener listener) {
        eventListeners.remove(listener);
    }
    
    // VoiceCommandListener implementation
    
    @Override
    public void onReadyForSpeech() {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onListeningEvent(ListeningEvent.READY);
        }
    }
    
    @Override
    public void onBeginningOfSpeech() {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onListeningEvent(ListeningEvent.SPEECH_START);
        }
    }
    
    @Override
    public void onEndOfSpeech() {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onListeningEvent(ListeningEvent.SPEECH_END);
        }
    }
    
    @Override
    public void onVolumeChanged(float rmsdB) {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onVolumeChanged(rmsdB);
        }
    }
    
    @Override
    public void onPartialResult(String partialResult) {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onPartialResult(partialResult);
        }
    }
    
    @Override
    public void onCommandReceived(String command) {
        // Add to history
        commandHistory.add(0, command);
        while (commandHistory.size() > MAX_HISTORY) {
            commandHistory.remove(commandHistory.size() - 1);
        }
        
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onCommandReceived(command);
        }
    }
    
    @Override
    public void onCommandProcessed(String command) {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onCommandProcessed(command);
        }
    }
    
    @Override
    public void onCommandNotRecognized(String command) {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onCommandNotRecognized(command);
        }
    }
    
    @Override
    public void onRecognitionError(String errorMessage) {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onError(errorMessage);
        }
    }
    
    @Override
    public void onListeningStarted() {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onListeningEvent(ListeningEvent.STARTED);
        }
    }
    
    @Override
    public void onListeningStopped() {
        // Notify listeners
        for (VoiceCommandEventListener listener : eventListeners) {
            listener.onListeningEvent(ListeningEvent.STOPPED);
        }
    }
    
    /**
     * Set up command categories
     */
    private void setupCommandCategories() {
        // System commands
        List<String> systemCommands = new ArrayList<>();
        systemCommands.add("start assistant");
        systemCommands.add("stop assistant");
        systemCommands.add("list commands");
        commandCategories.put("System", systemCommands);
        
        // Feature commands
        List<String> featureCommands = new ArrayList<>();
        featureCommands.add("enable * feature");
        featureCommands.add("disable * feature");
        commandCategories.put("Features", featureCommands);
        
        // Profile commands
        List<String> profileCommands = new ArrayList<>();
        profileCommands.add("switch to * profile");
        commandCategories.put("Profiles", profileCommands);
    }
    
    /**
     * Register feature control commands
     */
    private void registerFeatureControlCommands() {
        // Register commands for all features
        voiceCommandFeature.registerCommand("enable *", (command, context) -> {
            String featureName = voiceCommandFeature.extractWildcard(command, "enable *");
            return enableFeature(featureName);
        });
        
        voiceCommandFeature.registerCommand("disable *", (command, context) -> {
            String featureName = voiceCommandFeature.extractWildcard(command, "disable *");
            return disableFeature(featureName);
        });
    }
    
    /**
     * Enable a feature by name
     * @param featureName Feature name
     * @return true if feature was found and enabled
     */
    private boolean enableFeature(String featureName) {
        if (featureManager == null) {
            return false;
        }
        
        // Try to find the feature
        for (AIFeature feature : featureManager.getAllFeatures()) {
            if (feature.getName().equalsIgnoreCase(featureName) ||
                getNormalizedFeatureName(feature.getName()).equalsIgnoreCase(featureName)) {
                
                feature.setEnabled(true);
                Log.d(TAG, "Enabled feature: " + feature.getName());
                return true;
            }
        }
        
        Log.d(TAG, "Feature not found: " + featureName);
        return false;
    }
    
    /**
     * Disable a feature by name
     * @param featureName Feature name
     * @return true if feature was found and disabled
     */
    private boolean disableFeature(String featureName) {
        if (featureManager == null) {
            return false;
        }
        
        // Try to find the feature
        for (AIFeature feature : featureManager.getAllFeatures()) {
            if (feature.getName().equalsIgnoreCase(featureName) ||
                getNormalizedFeatureName(feature.getName()).equalsIgnoreCase(featureName)) {
                
                feature.setEnabled(false);
                Log.d(TAG, "Disabled feature: " + feature.getName());
                return true;
            }
        }
        
        Log.d(TAG, "Feature not found: " + featureName);
        return false;
    }
    
    /**
     * Get normalized feature name (for voice commands)
     * @param featureName Original feature name
     * @return Normalized name
     */
    private String getNormalizedFeatureName(String featureName) {
        // Convert from feature ID format to spoken format
        return featureName.replace("_", " ");
    }
    
    /**
     * Listening event enum
     */
    public enum ListeningEvent {
        READY,
        STARTED,
        STOPPED,
        SPEECH_START,
        SPEECH_END
    }
    
    /**
     * Voice Command Event Listener interface
     */
    public interface VoiceCommandEventListener {
        /**
         * Called when a listening event occurs
         * @param event The event
         */
        void onListeningEvent(ListeningEvent event);
        
        /**
         * Called when volume changes
         * @param volume Volume level
         */
        void onVolumeChanged(float volume);
        
        /**
         * Called when a partial result is available
         * @param partialResult Partial text
         */
        void onPartialResult(String partialResult);
        
        /**
         * Called when a command is received
         * @param command Command text
         */
        void onCommandReceived(String command);
        
        /**
         * Called when a command is processed
         * @param command Command text
         */
        void onCommandProcessed(String command);
        
        /**
         * Called when a command is not recognized
         * @param command Command text
         */
        void onCommandNotRecognized(String command);
        
        /**
         * Called when an error occurs
         * @param error Error message
         */
        void onError(String error);
    }
}
