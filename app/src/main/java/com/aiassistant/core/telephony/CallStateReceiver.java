package com.aiassistant.core.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Broadcast receiver for monitoring phone call state changes
 */
public class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiver";
    
    // Listeners for call state changes
    private final List<CallStateListener> callStateListeners = new ArrayList<>();
    
    // Current call state
    private int currentCallState = TelephonyManager.CALL_STATE_IDLE;
    private String currentPhoneNumber = null;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        
        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            // Get the call state
            String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            
            int state = TelephonyManager.CALL_STATE_IDLE;
            
            if (stateStr != null) {
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
            }
            
            // If state changed, notify listeners
            if (state != currentCallState || 
                    (phoneNumber != null && !phoneNumber.equals(currentPhoneNumber))) {
                currentCallState = state;
                currentPhoneNumber = phoneNumber;
                
                // Notify listeners
                notifyCallStateChanged(phoneNumber, state);
                
                Log.d(TAG, "Call state changed: " + stateStr + ", number: " + phoneNumber);
            }
        } else if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // Get the outgoing phone number
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            
            // Update current state
            currentCallState = TelephonyManager.CALL_STATE_OFFHOOK;
            currentPhoneNumber = phoneNumber;
            
            // Notify listeners
            notifyCallStateChanged(phoneNumber, TelephonyManager.CALL_STATE_OFFHOOK);
            
            Log.d(TAG, "Outgoing call: " + phoneNumber);
        }
    }
    
    /**
     * Notify listeners of call state changes
     */
    private void notifyCallStateChanged(String phoneNumber, int callState) {
        for (CallStateListener listener : callStateListeners) {
            listener.onCallStateChanged(phoneNumber, callState);
        }
    }
    
    /**
     * Register a call state listener
     */
    public void registerCallStateListener(CallStateListener listener) {
        if (listener != null && !callStateListeners.contains(listener)) {
            callStateListeners.add(listener);
        }
    }
    
    /**
     * Unregister a call state listener
     */
    public void unregisterCallStateListener(CallStateListener listener) {
        callStateListeners.remove(listener);
    }
    
    /**
     * Get the current call state
     */
    public int getCurrentCallState() {
        return currentCallState;
    }
    
    /**
     * Get the current phone number
     */
    public String getCurrentPhoneNumber() {
        return currentPhoneNumber;
    }
    
    /**
     * Interface for call state change events
     */
    public interface CallStateListener {
        void onCallStateChanged(String phoneNumber, int callState);
    }
}
