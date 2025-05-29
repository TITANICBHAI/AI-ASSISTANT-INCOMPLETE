package com.aiassistant.services;

import android.content.Context;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiassistant.ai.features.call.CallHandlingSystem;
import com.aiassistant.ai.features.call.ResearchEnabledCallHandler;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.SentientVoiceSystem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * AI-powered call screening service that can automatically intercept, answer,
 * and handle incoming calls based on user preferences.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class AICallScreeningService extends CallScreeningService {
    private static final String TAG = "AICallScreeningService";
    
    private CallHandlingSystem callHandlingSystem;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    private ResearchEnabledCallHandler researchEnabledCallHandler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        callHandlingSystem = CallHandlingSystem.getInstance(this);
        aiStateManager = AIStateManager.getInstance(this);
        voiceSystem = SentientVoiceSystem.getInstance(this);
        researchEnabledCallHandler = ResearchEnabledCallHandler.getInstance(this);
        
        Log.d(TAG, "AI Call Screening Service created");
    }
    
    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        Log.d(TAG, "Screening call from: " + callDetails.getHandle().getSchemeSpecificPart());
        
        String phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
        String callerName = getCallerName(phoneNumber);
        
        // Get user preference for this caller
        boolean shouldAnswerAutomatically = shouldAnswerAutomatically(phoneNumber);
        boolean shouldRejectCall = shouldRejectCall(phoneNumber);
        
        // Create response
        CallResponse.Builder responseBuilder = new CallResponse.Builder();
        
        if (shouldRejectCall) {
            // Reject call
            responseBuilder.setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);
            
            Log.d(TAG, "Rejecting call from: " + phoneNumber);
        } else {
            // Allow call
            responseBuilder.setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);
            
            Log.d(TAG, "Allowing call from: " + phoneNumber);
            
            // Notify the call handling system about the incoming call
            callHandlingSystem.handleIncomingCall(phoneNumber, callerName);
            
            // If we should answer automatically, trigger answer procedure
            if (shouldAnswerAutomatically) {
                Log.d(TAG, "Will answer call automatically from: " + phoneNumber);
                
                // Need to use the InCallService to actually answer the call
                // This call screening service just allows the call to come through
                // We'll handle the auto-answer in AICallService
            }
        }
        
        // Send the response
        respondToCall(callDetails, responseBuilder.build());
    }
    
    /**
     * Get caller name from phone number
     */
    private String getCallerName(String phoneNumber) {
        // In a real implementation, this would query the contacts database
        // For now, just return "Unknown Caller"
        return "Unknown Caller";
    }
    
    /**
     * Determine if a call should be answered automatically
     */
    private boolean shouldAnswerAutomatically(String phoneNumber) {
        // Check user preferences to see if this call should be answered
        // Based on caller identity, time of day, etc.
        
        // For now, check if auto-answer is enabled globally
        String autoAnswerPref = aiStateManager.getUserPreference("call_auto_answer", "false");
        boolean globalAutoAnswer = Boolean.parseBoolean(autoAnswerPref);
        
        if (globalAutoAnswer) {
            // Also check whitelist
            String whitelistPref = aiStateManager.getUserPreference("call_auto_answer_whitelist", "");
            String[] whitelist = whitelistPref.split(",");
            
            for (String whitelistedNumber : whitelist) {
                if (phoneNumber.contains(whitelistedNumber) || whitelistedNumber.contains(phoneNumber)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Determine if a call should be rejected
     */
    private boolean shouldRejectCall(String phoneNumber) {
        // Check if call blocking is enabled
        String blockingEnabled = aiStateManager.getUserPreference("call_blocking_enabled", "false");
        
        if (Boolean.parseBoolean(blockingEnabled)) {
            // Check if this number is on the blocklist
            String blocklistPref = aiStateManager.getUserPreference("call_blocklist", "");
            String[] blocklist = blocklistPref.split(",");
            
            for (String blockedNumber : blocklist) {
                if (phoneNumber.contains(blockedNumber) || blockedNumber.contains(phoneNumber)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AI Call Screening Service destroyed");
    }
}