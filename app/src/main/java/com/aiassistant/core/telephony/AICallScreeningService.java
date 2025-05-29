package com.aiassistant.core.telephony;

import android.annotation.SuppressLint;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * Intelligent call screening service for handling inbound and outbound calls
 * with AI capabilities. This service intercepts calls and provides real-time
 * processing and monitoring for business calling features.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class AICallScreeningService extends CallScreeningService {
    private static final String TAG = "AICallScreeningService";
    
    private DuplexCallHandler duplexCallHandler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        duplexCallHandler = new DuplexCallHandler(this);
        duplexCallHandler.initialize();
        Log.d(TAG, "AICallScreeningService created");
    }
    
    @Override
    public void onScreenCall(Call.Details callDetails) {
        try {
            String phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
            boolean isIncoming = (callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING);
            
            Log.d(TAG, "Screening " + (isIncoming ? "incoming" : "outgoing") + 
                    " call from/to " + phoneNumber);
            
            // Check if this is a call we need to monitor
            if (duplexCallHandler.shouldMonitorCall(phoneNumber, isIncoming)) {
                // Allow the call but monitor it
                CallResponse.Builder responseBuilder = new CallResponse.Builder();
                responseBuilder.setDisallowCall(false);
                responseBuilder.setRejectCall(false);
                responseBuilder.setSilenceCall(false);
                
                // Set this for outgoing calls we want to handle with Duplex
                if (!isIncoming && duplexCallHandler.isDuplexEnabledForCall(phoneNumber)) {
                    responseBuilder.setSilenceCall(true); // Silence so we can handle audio
                }
                
                respondToCall(callDetails, responseBuilder.build());
                
                // Start monitoring the call
                if (isIncoming) {
                    duplexCallHandler.startIncomingCallMonitoring(phoneNumber);
                } else {
                    duplexCallHandler.startOutgoingCallMonitoring(phoneNumber);
                }
            } else {
                // No need to monitor, just allow normally
                CallResponse.Builder responseBuilder = new CallResponse.Builder();
                responseBuilder.setDisallowCall(false);
                responseBuilder.setRejectCall(false);
                responseBuilder.setSilenceCall(false);
                respondToCall(callDetails, responseBuilder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in call screening: " + e.getMessage());
            // Default safe behavior - allow the call
            CallResponse.Builder responseBuilder = new CallResponse.Builder();
            responseBuilder.setDisallowCall(false);
            responseBuilder.setRejectCall(false);
            responseBuilder.setSilenceCall(false);
            respondToCall(callDetails, responseBuilder.build());
        }
    }
    
    @Override
    public void onDestroy() {
        if (duplexCallHandler != null) {
            duplexCallHandler.shutdown();
        }
        super.onDestroy();
    }
}
