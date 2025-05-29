package com.aiassistant.core.telephony;

/**
 * Constants for video call profiles
 */
public class VideoProfile {
    /**
     * "Audio only" call profile.
     * No video is transmitted or received.
     */
    public static final int STATE_AUDIO_ONLY = 0;
    
    /**
     * Video is transmitted in "portrait" orientation.
     */
    public static final int STATE_TX_ENABLED = 1;
    
    /**
     * Video is received.
     */
    public static final int STATE_RX_ENABLED = 2;
    
    /**
     * Video is bidirectional.
     */
    public static final int STATE_BIDIRECTIONAL = STATE_TX_ENABLED | STATE_RX_ENABLED;
    
    /**
     * Prevent instantiation.
     */
    private VideoProfile() {
    }
}
