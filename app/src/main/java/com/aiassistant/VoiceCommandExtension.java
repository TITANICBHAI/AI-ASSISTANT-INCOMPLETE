package com.aiassistant;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.aiassistant.ai.features.voice.context.ContextualVoiceProcessor;

/**
 * Extension to MainActivity that integrates context-aware voice commands
 * and JEE question solving capabilities.
 */
public class VoiceCommandExtension {
    private static final String TAG = "VoiceCommandExt";
    
    // Context-aware voice processor
    private ContextualVoiceProcessor voiceProcessor;
    
    // Android context
    private Context context;
    
    // UI references
    private TextView resultText;
    
    /**
     * Constructor
     * @param context Android context
     * @param resultText TextView for response display
     */
    public VoiceCommandExtension(Context context, TextView resultText) {
        this.context = context;
        this.resultText = resultText;
        
        // Initialize voice processor
        voiceProcessor = new ContextualVoiceProcessor(context);
    }
    
    /**
     * Process a voice command with context awareness
     * @param command Voice command
     * @return True if processing was successful
     */
    public boolean processVoiceCommand(String command) {
        try {
            String response = voiceProcessor.processCommand(command);
            updateResponseText(response);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Error processing command: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
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
     * Reset the conversation context
     */
    public void resetContext() {
        voiceProcessor.resetContext();
    }
    
    /**
     * Get the current conversation domain
     * @return Current domain
     */
    public String getCurrentDomain() {
        return voiceProcessor.getConversationContext().getCurrentDomain();
    }
    
    /**
     * Get the current conversation subject
     * @return Current subject or null if none
     */
    public String getCurrentSubject() {
        return voiceProcessor.getConversationContext().getCurrentSubject();
    }
}
