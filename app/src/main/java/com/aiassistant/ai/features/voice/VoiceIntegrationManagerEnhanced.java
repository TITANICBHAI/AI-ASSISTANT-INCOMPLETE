package com.aiassistant.ai.features.voice;

import android.content.Context;

/**
 * Enhanced extension of VoiceIntegrationManager 
 * to expose needed fields for advanced voice features
 */
public class VoiceIntegrationManagerEnhanced extends VoiceIntegrationManager {
    
    /**
     * Constructor
     * @param context Application context
     * @param commandManager Voice command manager
     * @param responseManager Voice response manager
     */
    public VoiceIntegrationManagerEnhanced(Context context, 
                                        VoiceCommandManager commandManager,
                                        VoiceResponseManager responseManager) {
        super(context, commandManager, responseManager);
    }
    
    /**
     * Get command manager
     * @return Voice command manager
     */
    public VoiceCommandManager getCommandManager() {
        return commandManager;
    }
    
    /**
     * Get response manager
     * @return Voice response manager
     */
    public VoiceResponseManager getResponseManager() {
        return responseManager;
    }
}
