package com.aiassistant.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiassistant.core.ai.call.EmotionalCallHandlingService;
import com.aiassistant.core.voice.VoiceManager;

/**
 * Service for handling phone calls
 * Provides intelligent call handling with emotional intelligence
 */
public class CallHandlingService extends Service {
    private static final String TAG = "CallHandlingService";
    
    // Call state
    private int currentCallState = TelephonyManager.CALL_STATE_IDLE;
    private String currentCallNumber = null;
    private long currentCallStartTime = 0;
    
    // Components
    private EmotionalCallHandlingService emotionalCallHandlingService;
    private VoiceManager voiceManager;
    
    // Broadcast receiver for outgoing calls
    private OutgoingCallReceiver outgoingCallReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize components
        emotionalCallHandlingService = new EmotionalCallHandlingService(this);
        voiceManager = new VoiceManager(this);
        
        // Register outgoing call receiver
        outgoingCallReceiver = new OutgoingCallReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(outgoingCallReceiver, filter);
        
        Log.d(TAG, "CallHandlingService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CallHandlingService started");
        
        // Handle intent data
        if (intent != null) {
            String state = intent.getStringExtra("state");
            String phoneNumber = intent.getStringExtra("phoneNumber");
            
            if (state != null && phoneNumber != null) {
                handlePhoneStateChange(state, phoneNumber);
            }
        }
        
        // Make service sticky so it restarts if killed
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        // Unregister receiver
        unregisterReceiver(outgoingCallReceiver);
        
        // Release voice manager
        if (voiceManager != null) {
            voiceManager.release();
        }
        
        Log.d(TAG, "CallHandlingService destroyed");
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Handle phone state change
     * @param state Phone state
     * @param phoneNumber Phone number
     */
    private void handlePhoneStateChange(String state, String phoneNumber) {
        Log.d(TAG, "Phone state change: " + state + ", number: " + phoneNumber);
        
        // Handle incoming call
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            currentCallState = TelephonyManager.CALL_STATE_RINGING;
            currentCallNumber = phoneNumber;
            
            // Get contact name
            String contactName = getContactName(phoneNumber);
            
            // Handle with emotional service
            emotionalCallHandlingService.handleIncomingCall(phoneNumber, contactName);
            
            Log.d(TAG, "Incoming call from " + phoneNumber + 
                  (contactName != null ? " (" + contactName + ")" : ""));
        }
        // Handle call answered/started
        else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            // If was ringing, call was answered
            if (currentCallState == TelephonyManager.CALL_STATE_RINGING) {
                // Call answered
                Log.d(TAG, "Call answered: " + currentCallNumber);
            } 
            // If was idle, outgoing call
            else if (currentCallState == TelephonyManager.CALL_STATE_IDLE) {
                // Outgoing call
                currentCallNumber = phoneNumber;
                Log.d(TAG, "Outgoing call to: " + currentCallNumber);
            }
            
            // Update state
            currentCallState = TelephonyManager.CALL_STATE_OFFHOOK;
            currentCallStartTime = System.currentTimeMillis();
        }
        // Handle call ended
        else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // If was offhook, call ended
            if (currentCallState == TelephonyManager.CALL_STATE_OFFHOOK) {
                // Calculate call duration
                long callDuration = 0;
                if (currentCallStartTime > 0) {
                    callDuration = (System.currentTimeMillis() - currentCallStartTime) / 1000;
                }
                
                // Handle call ended
                if (currentCallNumber != null) {
                    emotionalCallHandlingService.handleCallEnded(currentCallNumber, callDuration);
                    Log.d(TAG, "Call ended with " + currentCallNumber + ", duration: " + callDuration + " seconds");
                }
            }
            
            // Reset state
            currentCallState = TelephonyManager.CALL_STATE_IDLE;
            currentCallNumber = null;
            currentCallStartTime = 0;
        }
        // Handle outgoing call
        else if (state.equals("OUTGOING")) {
            currentCallState = TelephonyManager.CALL_STATE_OFFHOOK;
            currentCallNumber = phoneNumber;
            currentCallStartTime = System.currentTimeMillis();
            
            // Get contact name
            String contactName = getContactName(phoneNumber);
            
            // Handle with emotional service
            emotionalCallHandlingService.handleOutgoingCall(phoneNumber, contactName);
            
            Log.d(TAG, "Outgoing call to " + phoneNumber + 
                  (contactName != null ? " (" + contactName + ")" : ""));
        }
        
        Log.d(TAG, "New call state: " + stateToString(currentCallState));
    }
    
    /**
     * Handle detected speech from caller
     * @param speechText Detected speech text
     * @return Response text (if needed)
     */
    public String handleCallerSpeech(String speechText) {
        // If no active call, ignore speech
        if (currentCallState != TelephonyManager.CALL_STATE_OFFHOOK || currentCallNumber == null) {
            Log.w(TAG, "No active call to handle speech");
            return null;
        }
        
        // Handle with emotional service
        return emotionalCallHandlingService.handleCallerSpeech(currentCallNumber, speechText);
    }
    
    /**
     * Generate response during call
     * @param prompt Prompt for response
     * @return Response text
     */
    public String generateResponseDuringCall(String prompt) {
        // If no active call, can't generate response
        if (currentCallState != TelephonyManager.CALL_STATE_OFFHOOK || currentCallNumber == null) {
            Log.w(TAG, "No active call to generate response");
            return null;
        }
        
        // Generate with emotional service
        return emotionalCallHandlingService.generateResponseDuringCall(currentCallNumber, prompt);
    }
    
    /**
     * Speak response during call
     * @param response Response text
     */
    public void speakResponseDuringCall(String response) {
        // If no active call, can't speak
        if (currentCallState != TelephonyManager.CALL_STATE_OFFHOOK || currentCallNumber == null) {
            Log.w(TAG, "No active call to speak response");
            return;
        }
        
        // Speak with voice manager
        voiceManager.speak(response);
        Log.d(TAG, "Speaking response during call: " + response);
    }
    
    /**
     * Get current call state
     * @return Call state
     */
    public int getCurrentCallState() {
        return currentCallState;
    }
    
    /**
     * Get current call phone number
     * @return Phone number
     */
    public String getCurrentCallNumber() {
        return currentCallNumber;
    }
    
    /**
     * Get current call duration
     * @return Call duration in seconds
     */
    public long getCurrentCallDuration() {
        if (currentCallStartTime > 0) {
            return (System.currentTimeMillis() - currentCallStartTime) / 1000;
        }
        return 0;
    }
    
    /**
     * Get contact name for phone number
     * @param phoneNumber Phone number
     * @return Contact name or null if not found
     */
    private String getContactName(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }
        
        try {
            // Query contacts
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact name: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Convert call state to string
     * @param state Call state
     * @return State string
     */
    private String stateToString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "IDLE";
            case TelephonyManager.CALL_STATE_RINGING:
                return "RINGING";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "OFFHOOK";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Outgoing call broadcast receiver
     */
    private class OutgoingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get outgoing number
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Save phone number
                currentCallNumber = phoneNumber;
                
                // Get contact name
                String contactName = getContactName(phoneNumber);
                
                // Handle with emotional service
                emotionalCallHandlingService.handleOutgoingCall(phoneNumber, contactName);
                
                Log.d(TAG, "Outgoing call to " + phoneNumber);
            }
        }
    }
}
