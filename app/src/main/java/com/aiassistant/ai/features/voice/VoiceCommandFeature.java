package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Voice Command Integration Feature
 * - Enables hands-free control via voice commands
 * - Natural language command processing
 * - Custom command definitions
 * - Context-aware command interpretation
 */
public class VoiceCommandFeature extends BaseFeature {
    private static final String TAG = "VoiceCommand";
    private static final String FEATURE_NAME = "voice_command_integration";
    
    // Speech recognizer
    private SpeechRecognizer speechRecognizer;
    
    // Speech recognition listener
    private SpeechRecognitionListener recognitionListener;
    
    // Recognition intent
    private Intent recognizerIntent;
    
    // Command processor
    private final CommandProcessor commandProcessor;
    
    // Command registry
    private final Map<String, CommandHandler> commandRegistry;
    
    // Custom commands
    private final Map<String, String> customCommands;
    
    // Listeners for voice command events
    private final List<VoiceCommandListener> commandListeners;
    
    // Voice activation state
    private boolean isListening;
    
    // Current recognition language
    private String currentLanguage;
    
    // Command execution context
    private final CommandContext commandContext;
    
    /**
     * Constructor
     * @param context Application context
     */
    public VoiceCommandFeature(Context context) {
        super(context, FEATURE_NAME);
        this.commandProcessor = new CommandProcessor();
        this.commandRegistry = new HashMap<>();
        this.customCommands = new HashMap<>();
        this.commandListeners = new CopyOnWriteArrayList<>();
        this.isListening = false;
        this.currentLanguage = "en-US";
        this.commandContext = new CommandContext();
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Initialize speech recognizer
                initializeSpeechRecognizer();
                
                // Register default commands
                registerDefaultCommands();
                
                Log.d(TAG, "Voice command system initialized with " + 
                      commandRegistry.size() + " commands");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize voice command system", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled()) return;
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update command context
            updateCommandContext();
        } catch (Exception e) {
            Log.e(TAG, "Error updating voice command system", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Stop listening
        stopListening();
        
        // Release speech recognizer
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        
        // Clear listeners
        commandListeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start listening for voice commands
     */
    public void startListening() {
        if (!isEnabled() || isListening || speechRecognizer == null) {
            return;
        }
        
        try {
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
            
            Log.d(TAG, "Started listening for voice commands");
            
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onListeningStarted();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting voice recognition", e);
        }
    }
    
    /**
     * Stop listening for voice commands
     */
    public void stopListening() {
        if (!isEnabled() || !isListening || speechRecognizer == null) {
            return;
        }
        
        try {
            speechRecognizer.stopListening();
            isListening = false;
            
            Log.d(TAG, "Stopped listening for voice commands");
            
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onListeningStopped();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping voice recognition", e);
        }
    }
    
    /**
     * Check if currently listening
     * @return true if listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Set recognition language
     * @param languageCode Language code (e.g., "en-US")
     */
    public void setLanguage(String languageCode) {
        this.currentLanguage = languageCode;
        
        // Update recognizer intent
        if (recognizerIntent != null) {
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
        }
        
        Log.d(TAG, "Set recognition language to " + languageCode);
    }
    
    /**
     * Get current recognition language
     * @return Language code
     */
    public String getLanguage() {
        return currentLanguage;
    }
    
    /**
     * Register a command handler
     * @param commandPattern Command pattern (can include wildcards)
     * @param handler Command handler
     */
    public void registerCommand(String commandPattern, CommandHandler handler) {
        if (commandPattern != null && handler != null) {
            commandRegistry.put(commandPattern.toLowerCase(), handler);
            Log.d(TAG, "Registered command: " + commandPattern);
        }
    }
    
    /**
     * Unregister a command
     * @param commandPattern Command pattern
     */
    public void unregisterCommand(String commandPattern) {
        if (commandPattern != null) {
            commandRegistry.remove(commandPattern.toLowerCase());
            Log.d(TAG, "Unregistered command: " + commandPattern);
        }
    }
    
    /**
     * Add a custom command alias
     * @param alias Alias phrase
     * @param command Actual command
     */
    public void addCustomCommand(String alias, String command) {
        if (alias != null && command != null) {
            customCommands.put(alias.toLowerCase(), command);
            Log.d(TAG, "Added custom command: " + alias + " -> " + command);
        }
    }
    
    /**
     * Remove a custom command
     * @param alias Alias to remove
     */
    public void removeCustomCommand(String alias) {
        if (alias != null) {
            customCommands.remove(alias.toLowerCase());
            Log.d(TAG, "Removed custom command: " + alias);
        }
    }
    
    /**
     * Get all registered commands
     * @return List of command patterns
     */
    public List<String> getRegisteredCommands() {
        return new ArrayList<>(commandRegistry.keySet());
    }
    
    /**
     * Get all custom commands
     * @return Map of alias -> command
     */
    public Map<String, String> getCustomCommands() {
        return new HashMap<>(customCommands);
    }
    
    /**
     * Process a text command directly
     * @param command Text command
     * @return true if command was handled
     */
    public boolean processTextCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        
        Log.d(TAG, "Processing text command: " + command);
        
        // Notify listeners
        for (VoiceCommandListener listener : commandListeners) {
            listener.onCommandReceived(command);
        }
        
        // Process the command
        boolean handled = commandProcessor.processCommand(command);
        
        // Notify listeners
        if (handled) {
            for (VoiceCommandListener listener : commandListeners) {
                listener.onCommandProcessed(command);
            }
        } else {
            for (VoiceCommandListener listener : commandListeners) {
                listener.onCommandNotRecognized(command);
            }
        }
        
        return handled;
    }
    
    /**
     * Add a voice command listener
     * @param listener Listener to add
     */
    public void addCommandListener(VoiceCommandListener listener) {
        if (listener != null && !commandListeners.contains(listener)) {
            commandListeners.add(listener);
        }
    }
    
    /**
     * Remove a voice command listener
     * @param listener Listener to remove
     */
    public void removeCommandListener(VoiceCommandListener listener) {
        commandListeners.remove(listener);
    }
    
    /**
     * Set a context variable
     * @param key Variable key
     * @param value Variable value
     */
    public void setContextVariable(String key, Object value) {
        commandContext.setVariable(key, value);
    }
    
    /**
     * Get a context variable
     * @param key Variable key
     * @return Variable value or null if not found
     */
    public Object getContextVariable(String key) {
        return commandContext.getVariable(key);
    }
    
    /**
     * Initialize speech recognizer
     */
    private void initializeSpeechRecognizer() {
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(getContext())) {
            Log.e(TAG, "Speech recognition not available on this device");
            return;
        }
        
        // Create speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        
        // Create recognition listener
        recognitionListener = new SpeechRecognitionListener();
        speechRecognizer.setRecognitionListener(recognitionListener);
        
        // Create recognizer intent
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }
    
    /**
     * Register default commands
     */
    private void registerDefaultCommands() {
        // Start/Stop AI commands
        registerCommand("start assistant", (command, context) -> {
            // Implementation would start AI
            Log.d(TAG, "Executing: Start assistant");
            return true;
        });
        
        registerCommand("stop assistant", (command, context) -> {
            // Implementation would stop AI
            Log.d(TAG, "Executing: Stop assistant");
            return true;
        });
        
        // Feature control commands
        registerCommand("enable * feature", (command, context) -> {
            String featureName = extractWildcard(command, "enable * feature");
            // Implementation would enable feature
            Log.d(TAG, "Executing: Enable feature " + featureName);
            return true;
        });
        
        registerCommand("disable * feature", (command, context) -> {
            String featureName = extractWildcard(command, "disable * feature");
            // Implementation would disable feature
            Log.d(TAG, "Executing: Disable feature " + featureName);
            return true;
        });
        
        // Profile commands
        registerCommand("switch to * profile", (command, context) -> {
            String profileName = extractWildcard(command, "switch to * profile");
            // Implementation would switch profile
            Log.d(TAG, "Executing: Switch to profile " + profileName);
            return true;
        });
        
        // Help command
        registerCommand("list commands", (command, context) -> {
            // Implementation would list available commands
            Log.d(TAG, "Executing: List commands");
            return true;
        });
    }
    
    /**
     * Update command context with current state
     */
    private void updateCommandContext() {
        // This would update context variables based on current state
        // For now, just a stub
    }
    
    /**
     * Extract wildcard text from command
     * @param command Full command
     * @param pattern Pattern with wildcard (*)
     * @return Extracted text or empty string
     */
    private String extractWildcard(String command, String pattern) {
        if (command == null || pattern == null) {
            return "";
        }
        
        String[] patternParts = pattern.split("\\*", 2);
        if (patternParts.length != 2) {
            return "";
        }
        
        String prefix = patternParts[0].trim();
        String suffix = patternParts[1].trim();
        
        if (!command.startsWith(prefix)) {
            return "";
        }
        
        String remainder = command.substring(prefix.length()).trim();
        
        if (suffix.isEmpty()) {
            return remainder;
        }
        
        if (!remainder.endsWith(suffix)) {
            return "";
        }
        
        return remainder.substring(0, remainder.length() - suffix.length()).trim();
    }
    
    /**
     * Speech Recognition Listener
     * Handles speech recognition events
     */
    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onReadyForSpeech();
            }
        }
        
        @Override
        public void onBeginningOfSpeech() {
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onBeginningOfSpeech();
            }
        }
        
        @Override
        public void onRmsChanged(float rmsdB) {
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onVolumeChanged(rmsdB);
            }
        }
        
        @Override
        public void onBufferReceived(byte[] buffer) {
            // Not used
        }
        
        @Override
        public void onEndOfSpeech() {
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onEndOfSpeech();
            }
        }
        
        @Override
        public void onError(int error) {
            String errorMessage = getErrorMessage(error);
            Log.e(TAG, "Speech recognition error: " + errorMessage);
            
            // Notify listeners
            for (VoiceCommandListener listener : commandListeners) {
                listener.onRecognitionError(errorMessage);
            }
            
            // Restart listening if appropriate
            if (isEnabled() && isListening) {
                startListening();
            }
        }
        
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String bestMatch = matches.get(0);
                
                // Process the command
                processTextCommand(bestMatch);
            }
            
            // Restart listening if appropriate
            if (isEnabled() && isListening) {
                startListening();
            }
        }
        
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String partialText = matches.get(0);
                
                // Notify listeners
                for (VoiceCommandListener listener : commandListeners) {
                    listener.onPartialResult(partialText);
                }
            }
        }
        
        @Override
        public void onEvent(int eventType, Bundle params) {
            // Not used
        }
        
        /**
         * Get error message for error code
         * @param errorCode Error code
         * @return Error message
         */
        private String getErrorMessage(int errorCode) {
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    return "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    return "Client side error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    return "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    return "Network error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    return "Network timeout";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    return "No recognition matches";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    return "Recognition service busy";
                case SpeechRecognizer.ERROR_SERVER:
                    return "Server error";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    return "No speech input";
                default:
                    return "Unknown error (" + errorCode + ")";
            }
        }
    }
    
    /**
     * Command Processor
     * Processes voice commands
     */
    private class CommandProcessor {
        /**
         * Process a command
         * @param command Command text
         * @return true if command was handled
         */
        public boolean processCommand(String command) {
            if (command == null || command.isEmpty()) {
                return false;
            }
            
            // Convert to lowercase for case-insensitive matching
            String lowerCommand = command.toLowerCase();
            
            // Check for custom command
            if (customCommands.containsKey(lowerCommand)) {
                // Replace with actual command
                lowerCommand = customCommands.get(lowerCommand);
            }
            
            // Try exact match first
            if (commandRegistry.containsKey(lowerCommand)) {
                return executeCommand(lowerCommand, commandRegistry.get(lowerCommand));
            }
            
            // Try wildcard patterns
            for (Map.Entry<String, CommandHandler> entry : commandRegistry.entrySet()) {
                String pattern = entry.getKey();
                
                if (pattern.contains("*") && matchesWildcardPattern(lowerCommand, pattern)) {
                    return executeCommand(lowerCommand, entry.getValue());
                }
            }
            
            // No handler found
            Log.d(TAG, "No handler found for command: " + command);
            return false;
        }
        
        /**
         * Execute a command
         * @param command Command text
         * @param handler Command handler
         * @return true if command was handled
         */
        private boolean executeCommand(String command, CommandHandler handler) {
            try {
                return handler.handleCommand(command, commandContext);
            } catch (Exception e) {
                Log.e(TAG, "Error executing command: " + command, e);
                return false;
            }
        }
        
        /**
         * Check if command matches wildcard pattern
         * @param command Command text
         * @param pattern Pattern with wildcard
         * @return true if matches
         */
        private boolean matchesWildcardPattern(String command, String pattern) {
            String[] parts = pattern.split("\\*", 2);
            
            if (parts.length != 2) {
                return false;
            }
            
            String prefix = parts[0].trim();
            String suffix = parts[1].trim();
            
            return command.startsWith(prefix) && 
                  (suffix.isEmpty() || command.endsWith(suffix));
        }
    }
    
    /**
     * Command Context
     * Holds context for command execution
     */
    public class CommandContext {
        private final Map<String, Object> variables;
        
        /**
         * Constructor
         */
        public CommandContext() {
            this.variables = new HashMap<>();
        }
        
        /**
         * Set a variable
         * @param key Variable key
         * @param value Variable value
         */
        public void setVariable(String key, Object value) {
            variables.put(key, value);
        }
        
        /**
         * Get a variable
         * @param key Variable key
         * @return Variable value or null if not found
         */
        public Object getVariable(String key) {
            return variables.get(key);
        }
        
        /**
         * Get a variable as string
         * @param key Variable key
         * @param defaultValue Default value if not found
         * @return Variable value or default value
         */
        public String getString(String key, String defaultValue) {
            Object value = variables.get(key);
            return (value != null) ? value.toString() : defaultValue;
        }
        
        /**
         * Get a variable as boolean
         * @param key Variable key
         * @param defaultValue Default value if not found
         * @return Variable value or default value
         */
        public boolean getBoolean(String key, boolean defaultValue) {
            Object value = variables.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return defaultValue;
        }
        
        /**
         * Get a variable as integer
         * @param key Variable key
         * @param defaultValue Default value if not found
         * @return Variable value or default value
         */
        public int getInteger(String key, int defaultValue) {
            Object value = variables.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        }
        
        /**
         * Get a variable as float
         * @param key Variable key
         * @param defaultValue Default value if not found
         * @return Variable value or default value
         */
        public float getFloat(String key, float defaultValue) {
            Object value = variables.get(key);
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return defaultValue;
        }
        
        /**
         * Check if a variable exists
         * @param key Variable key
         * @return true if exists
         */
        public boolean hasVariable(String key) {
            return variables.containsKey(key);
        }
        
        /**
         * Remove a variable
         * @param key Variable key
         */
        public void removeVariable(String key) {
            variables.remove(key);
        }
        
        /**
         * Clear all variables
         */
        public void clearVariables() {
            variables.clear();
        }
    }
    
    /**
     * Command Handler interface
     * For handling voice commands
     */
    public interface CommandHandler {
        /**
         * Handle a command
         * @param command Command text
         * @param context Command context
         * @return true if command was handled
         */
        boolean handleCommand(String command, CommandContext context);
    }
    
    /**
     * Voice Command Listener interface
     * For receiving voice command events
     */
    public interface VoiceCommandListener {
        /**
         * Called when recognition is ready for speech
         */
        void onReadyForSpeech();
        
        /**
         * Called when speech begins
         */
        void onBeginningOfSpeech();
        
        /**
         * Called when speech ends
         */
        void onEndOfSpeech();
        
        /**
         * Called when recognition volume changes
         * @param rmsdB Volume level
         */
        void onVolumeChanged(float rmsdB);
        
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
         * Called when a recognition error occurs
         * @param errorMessage Error message
         */
        void onRecognitionError(String errorMessage);
        
        /**
         * Called when listening starts
         */
        void onListeningStarted();
        
        /**
         * Called when listening stops
         */
        void onListeningStopped();
    }
}
