package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Voice Command UI
 * Example implementation of a voice command UI
 */
public class VoiceCommandUI implements VoiceCommandManager.VoiceCommandEventListener {
    private static final String TAG = "VoiceCommandUI";
    
    private final Context context;
    private final VoiceCommandManager commandManager;
    private final List<VoiceUIListener> uiListeners;
    
    // UI state
    private boolean isListening;
    private float currentVolume;
    private String currentPartialText;
    private String lastCommand;
    private String lastErrorMessage;
    
    /**
     * Constructor
     * @param context Application context
     * @param commandManager Voice command manager
     */
    public VoiceCommandUI(Context context, VoiceCommandManager commandManager) {
        this.context = context;
        this.commandManager = commandManager;
        this.uiListeners = new ArrayList<>();
        this.isListening = false;
        this.currentVolume = 0.0f;
        this.currentPartialText = "";
        this.lastCommand = "";
        this.lastErrorMessage = "";
        
        // Register as listener
        commandManager.addEventListener(this);
    }
    
    /**
     * Toggle listening state
     * @return New listening state
     */
    public boolean toggleListening() {
        if (isListening) {
            commandManager.stopListening();
        } else {
            commandManager.startListening();
        }
        
        return isListening;
    }
    
    /**
     * Start listening
     */
    public void startListening() {
        commandManager.startListening();
    }
    
    /**
     * Stop listening
     */
    public void stopListening() {
        commandManager.stopListening();
    }
    
    /**
     * Submit a text command
     * @param commandText Command text
     */
    public void submitTextCommand(String commandText) {
        if (commandText != null && !commandText.isEmpty()) {
            commandManager.processCommand(commandText);
        }
    }
    
    /**
     * Get current listening state
     * @return true if listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Get current volume level
     * @return Volume level (0.0-1.0)
     */
    public float getCurrentVolume() {
        return currentVolume;
    }
    
    /**
     * Get current partial text
     * @return Partial text
     */
    public String getCurrentPartialText() {
        return currentPartialText;
    }
    
    /**
     * Get last recognized command
     * @return Last command
     */
    public String getLastCommand() {
        return lastCommand;
    }
    
    /**
     * Get last error message
     * @return Error message
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    /**
     * Get command history
     * @return Command history
     */
    public List<String> getCommandHistory() {
        return commandManager.getCommandHistory();
    }
    
    /**
     * Get all command categories
     * @return List of categories
     */
    public List<String> getCommandCategories() {
        return commandManager.getCommandCategories();
    }
    
    /**
     * Get commands in a category
     * @param category Category name
     * @return List of commands
     */
    public List<String> getCommandsInCategory(String category) {
        return commandManager.getCommandsByCategory(category);
    }
    
    /**
     * Add a UI listener
     * @param listener UI listener
     */
    public void addUIListener(VoiceUIListener listener) {
        if (listener != null && !uiListeners.contains(listener)) {
            uiListeners.add(listener);
        }
    }
    
    /**
     * Remove a UI listener
     * @param listener UI listener
     */
    public void removeUIListener(VoiceUIListener listener) {
        uiListeners.remove(listener);
    }
    
    @Override
    public void onListeningEvent(VoiceCommandManager.ListeningEvent event) {
        switch (event) {
            case STARTED:
                isListening = true;
                showToast("Listening started");
                break;
                
            case STOPPED:
                isListening = false;
                showToast("Listening stopped");
                break;
                
            case READY:
                showToast("Ready for speech");
                break;
                
            case SPEECH_START:
                showToast("Speech detected");
                break;
                
            case SPEECH_END:
                showToast("Processing speech");
                break;
        }
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onListeningStateChanged(isListening);
        }
    }
    
    @Override
    public void onVolumeChanged(float volume) {
        // Normalize volume to 0.0-1.0
        this.currentVolume = Math.min(1.0f, Math.max(0.0f, volume / 10.0f));
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onVolumeChanged(currentVolume);
        }
    }
    
    @Override
    public void onPartialResult(String partialResult) {
        this.currentPartialText = partialResult;
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onPartialTextChanged(currentPartialText);
        }
    }
    
    @Override
    public void onCommandReceived(String command) {
        this.lastCommand = command;
        this.currentPartialText = "";
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onCommandReceived(command);
        }
    }
    
    @Override
    public void onCommandProcessed(String command) {
        showToast("Command processed: " + command);
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onCommandProcessed(command);
        }
    }
    
    @Override
    public void onCommandNotRecognized(String command) {
        showToast("Command not recognized: " + command);
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onCommandNotRecognized(command);
        }
    }
    
    @Override
    public void onError(String error) {
        this.lastErrorMessage = error;
        showToast("Error: " + error);
        
        // Notify listeners
        for (VoiceUIListener listener : uiListeners) {
            listener.onError(error);
        }
    }
    
    /**
     * Show a toast message
     * @param message Message to show
     */
    private void showToast(String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }
    
    /**
     * Voice UI Listener interface
     */
    public interface VoiceUIListener {
        /**
         * Called when listening state changes
         * @param isListening New listening state
         */
        void onListeningStateChanged(boolean isListening);
        
        /**
         * Called when volume changes
         * @param volume Volume level (0.0-1.0)
         */
        void onVolumeChanged(float volume);
        
        /**
         * Called when partial text changes
         * @param partialText Partial text
         */
        void onPartialTextChanged(String partialText);
        
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
