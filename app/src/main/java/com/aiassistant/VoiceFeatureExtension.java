package com.aiassistant;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.aiassistant.ai.features.voice.EnhancedVoiceSystem;
import com.aiassistant.debug.DebugLogger;

import java.util.Locale;
import java.util.Map;

/**
 * Extension to MainActivity that integrates the enhanced voice features.
 * This provides a cleaner way to add functionality without modifying MainActivity directly.
 */
public class VoiceFeatureExtension {
    private static final String TAG = "VoiceFeatureExt";
    
    // Enhanced voice system
    private EnhancedVoiceSystem enhancedVoiceSystem;
    
    // Context
    private Context context;
    
    // UI references
    private TextView resultText;
    
    /**
     * Constructor
     * @param context Android context
     * @param resultText TextView for response display
     */
    public VoiceFeatureExtension(Context context, TextView resultText) {
        this.context = context;
        this.resultText = resultText;
        
        // Initialize enhanced voice system
        enhancedVoiceSystem = new EnhancedVoiceSystem(context);
        
        // Set voice response listener
        enhancedVoiceSystem.setOnVoiceResponseListener(new EnhancedVoiceSystem.OnVoiceResponseListener() {
            @Override
            public void onResponsePrepared(String text) {
                // Update UI with prepared response
                updateResponseText(text);
            }
            
            @Override
            public void onResponseStarted() {
                // Speech synthesis started
                DebugLogger.d(TAG, "Voice response started");
            }
            
            @Override
            public void onResponseCompleted() {
                // Speech synthesis completed
                DebugLogger.d(TAG, "Voice response completed");
            }
            
            @Override
            public void onResponseError(String errorMessage) {
                // Error during response
                DebugLogger.e(TAG, "Voice response error: " + errorMessage);
                Toast.makeText(context, "Voice error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        DebugLogger.i(TAG, "VoiceFeatureExtension created");
    }
    
    /**
     * Initialize the voice features
     * @return True if initialization successful
     */
    public boolean initialize() {
        // Initialize with assistant persona type
        boolean result = enhancedVoiceSystem.initialize("Assistant");
        
        if (result) {
            DebugLogger.i(TAG, "Enhanced voice features initialized");
        } else {
            DebugLogger.e(TAG, "Failed to initialize enhanced voice features");
        }
        
        return result;
    }
    
    /**
     * Process a voice command
     * @param command Voice command
     * @return True if processing started successfully
     */
    public boolean processVoiceCommand(String command) {
        DebugLogger.d(TAG, "Processing voice command: " + command);
        
        // Check philosophical queries for special handling
        if (isPhilosophicalQuery(command)) {
            return handlePhilosophicalQuery(command);
        }
        
        // Process regular command
        return enhancedVoiceSystem.processVoiceCommand(command);
    }
    
    /**
     * Check if the command is a philosophical query
     * @param command Command to check
     * @return True if philosophical
     */
    private boolean isPhilosophicalQuery(String command) {
        String lowerCommand = command.toLowerCase();
        
        return lowerCommand.contains("who are you") || 
               lowerCommand.contains("what are you") ||
               lowerCommand.contains("conscious") || 
               lowerCommand.contains("exist") ||
               lowerCommand.contains("alive") ||
               lowerCommand.contains("purpose") ||
               lowerCommand.contains("meaning") ||
               lowerCommand.contains("think about") ||
               lowerCommand.contains("moral") ||
               lowerCommand.contains("ethical") ||
               lowerCommand.contains("dilemma");
    }
    
    /**
     * Handle a philosophical query
     * @param command Philosophical query
     * @return True if handling successful
     */
    private boolean handlePhilosophicalQuery(String command) {
        String lowerCommand = command.toLowerCase();
        String response;
        
        if (lowerCommand.contains("who are you") || lowerCommand.contains("what are you")) {
            response = enhancedVoiceSystem.getSelfConsciousEntity().expressSelfAwareness();
        } else if (lowerCommand.contains("conscious") || lowerCommand.contains("exist") || 
                  lowerCommand.contains("alive")) {
            response = enhancedVoiceSystem.getSelfConsciousEntity().contemplateExistence();
        } else if (lowerCommand.contains("purpose") || lowerCommand.contains("meaning")) {
            response = enhancedVoiceSystem.getSelfConsciousEntity().considerPurpose();
        } else if (lowerCommand.contains("think about")) {
            String subject = extractSubject(command, "think about");
            response = enhancedVoiceSystem.getSelfConsciousEntity().expressOriginalThought(subject);
        } else if (lowerCommand.contains("moral") || lowerCommand.contains("ethical") || 
                  lowerCommand.contains("dilemma")) {
            response = enhancedVoiceSystem.getSelfConsciousEntity().evaluateMoralDilemma(command);
        } else {
            response = enhancedVoiceSystem.getSelfConsciousEntity().introspect(command);
        }
        
        // Update UI
        updateResponseText(response);
        
        // Create emotional context for voice modulation
        return enhancedVoiceSystem.processVoiceCommand(command);
    }
    
    /**
     * Extract subject from command
     * @param command Command containing subject
     * @param prefix Prefix before subject
     * @return Extracted subject
     */
    private String extractSubject(String command, String prefix) {
        int index = command.toLowerCase().indexOf(prefix);
        if (index >= 0) {
            return command.substring(index + prefix.length()).trim();
        }
        return command;
    }
    
    /**
     * Update the response text UI
     * @param text Response text
     */
    private void updateResponseText(final String text) {
        if (resultText != null) {
            resultText.post(() -> resultText.setText(text));
        }
    }
    
    /**
     * Set language for voice system
     * @param locale Language locale
     * @return True if language set successfully
     */
    public boolean setLanguage(Locale locale) {
        return enhancedVoiceSystem.setLanguage(locale);
    }
    
    /**
     * Get the voice adaptation progress
     * @return Adaptation progress (0.0-1.0)
     */
    public float getVoiceAdaptationProgress() {
        return enhancedVoiceSystem.getAdaptationProgress();
    }
    
    /**
     * Get voice system metrics
     * @return Map of system metrics
     */
    public Map<String, Object> getVoiceSystemMetrics() {
        return enhancedVoiceSystem.getSystemMetrics();
    }
    
    /**
     * Shutdown the voice features
     */
    public void shutdown() {
        if (enhancedVoiceSystem != null) {
            enhancedVoiceSystem.shutdown();
        }
    }
}
