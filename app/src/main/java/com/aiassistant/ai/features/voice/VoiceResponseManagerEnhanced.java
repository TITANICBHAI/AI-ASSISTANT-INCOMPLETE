package com.aiassistant.ai.features.voice;

import android.content.Context;
import java.util.List;

/**
 * Enhanced extension of VoiceResponseManager to
 * provide additional functionality
 */
public class VoiceResponseManagerEnhanced extends VoiceResponseManager {
    
    /**
     * Constructor
     * @param context Application context
     * @param voiceResponseFeature Voice response feature
     */
    public VoiceResponseManagerEnhanced(Context context, VoiceResponseFeature voiceResponseFeature) {
        super(context, voiceResponseFeature);
    }
    
    /**
     * Remove all voice characteristics
     */
    public void removeAllCharacteristics() {
        List<String> characteristics = getVoiceCharacteristics();
        for (String characteristic : characteristics) {
            removeVoiceCharacteristic(characteristic);
        }
    }
    
    /**
     * Add a voice characteristic
     * @param characteristic Characteristic to add
     */
    public void addCharacteristic(String characteristic) {
        addVoiceCharacteristic(characteristic);
    }
    
    /**
     * Get voice response feature
     * @return Voice response feature
     */
    public VoiceResponseFeature getVoiceResponseFeature() {
        return voiceResponseFeature;
    }
}
