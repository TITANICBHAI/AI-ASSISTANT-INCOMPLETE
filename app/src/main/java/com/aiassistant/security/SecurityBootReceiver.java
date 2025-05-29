package com.aiassistant.security;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * SecurityBootReceiver is a BroadcastReceiver that listens for system boot events
 * and starts the anti-detection system if it was active before device restart.
 * 
 * This allows the security features to persist across device restarts.
 */
public class SecurityBootReceiver extends BroadcastReceiver {
    private static final String TAG = "SecurityBootReceiver";
    private static final String PREFS_NAME = "security_prefs";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && 
                (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || 
                 intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON") ||
                 intent.getAction().equals("com.htc.intent.action.QUICKBOOT_POWERON"))) {
            
            Log.d(TAG, "Device boot completed");
            
            // Check if protection was active before device restart
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean wasProtectionActive = preferences.getBoolean("protection_active", false);
            int protectionLevel = preferences.getInt("protection_level", 1);
            
            if (wasProtectionActive && protectionLevel > 0) {
                Log.d(TAG, "Restarting protection with level " + protectionLevel);
                
                // Get the AntiDetectionManager instance
                AntiDetectionManager securityManager = AntiDetectionManager.getInstance(context);
                
                // Set protection level
                securityManager.setProtectionLevel(protectionLevel);
                
                // Activate protection
                securityManager.activateProtection();
                
                // Start the anti-detection service explicitly
                startAntiDetectionService(context, protectionLevel);
            }
        }
    }
    
    /**
     * Start the anti-detection service
     */
    private void startAntiDetectionService(Context context, int protectionLevel) {
        try {
            Intent serviceIntent = new Intent(context, AntiDetectionService.class);
            serviceIntent.putExtra("protection_level", protectionLevel);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            Log.d(TAG, "Anti-detection service started from boot receiver");
        } catch (Exception e) {
            Log.e(TAG, "Error starting anti-detection service: " + e.getMessage());
        }
    }
}
