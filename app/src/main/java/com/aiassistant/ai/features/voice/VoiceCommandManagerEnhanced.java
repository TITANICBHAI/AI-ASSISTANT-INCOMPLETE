package com.aiassistant.ai.features.voice;

import android.content.Context;

/**
 * Enhanced extension of VoiceCommandManager to expose needed fields
 * for advanced voice features
 */
public class VoiceCommandManagerEnhanced extends VoiceCommandManager {
    
    /**
     * Constructor
     * @param context Application context
     * @param voiceCommandFeature Voice command feature
     */
    public VoiceCommandManagerEnhanced(Context context, VoiceCommandFeature voiceCommandFeature) {
        super(context, voiceCommandFeature);
    }
    
    /**
     * Get voice command feature
     * @return Voice command feature
     */
    public VoiceCommandFeature getVoiceCommandFeature() {
        return voiceCommandFeature;
    }
}
