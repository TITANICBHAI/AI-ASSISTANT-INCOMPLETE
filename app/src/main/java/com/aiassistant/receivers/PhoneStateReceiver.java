package com.aiassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiassistant.services.CallHandlingService;

/**
 * Broadcast receiver for phone state changes
 * Handles incoming and outgoing calls
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneStateReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Phone state changed: " + intent.getAction());
        
        // Handle phone state changes
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            
            Log.d(TAG, "Phone state: " + state + ", number: " + phoneNumber);
            
            // Start CallHandlingService with phone state information
            Intent serviceIntent = new Intent(context, CallHandlingService.class);
            serviceIntent.putExtra("state", state);
            serviceIntent.putExtra("phoneNumber", phoneNumber);
            context.startService(serviceIntent);
        }
        // Handle outgoing calls
        else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            
            Log.d(TAG, "Outgoing call to: " + phoneNumber);
            
            // Start CallHandlingService with outgoing call information
            Intent serviceIntent = new Intent(context, CallHandlingService.class);
            serviceIntent.putExtra("state", "OUTGOING");
            serviceIntent.putExtra("phoneNumber", phoneNumber);
            context.startService(serviceIntent);
        }
    }
}
