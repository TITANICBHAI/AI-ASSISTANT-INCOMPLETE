package com.aiassistant.core.voice.authentication;

import android.content.Context;
import android.util.Log;

/**
 * Handles voice biometric authentication operations
 */
public class VoiceBiometricAuthenticator {
    private static final String TAG = "VoiceBiometricAuth";
    
    // Application context
    private final Context context;
    
    /**
     * Constructor
     * @param context Application context
     */
    public VoiceBiometricAuthenticator(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Types of alternative authentication
     */
    public enum AlternativeAuthType {
        SECURITY_QUESTION,    // Security question answers
        PASSCODE,             // Numeric or alphanumeric passcode
        MANUAL_SENTENCE,      // Manually typing the authentication sentence
        OTHER                 // Other authentication types
    }
}
