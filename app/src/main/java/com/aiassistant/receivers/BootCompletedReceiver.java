package com.aiassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aiassistant.core.ai.memory.MemoryService;
import com.aiassistant.services.CallHandlingService;

/**
 * Broadcast receiver for device boot completion
 * Starts required services when device boots
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Device boot completed, starting services");
            
            // Start memory service
            Intent memoryServiceIntent = new Intent(context, MemoryService.class);
            context.startService(memoryServiceIntent);
            
            // Start call handling service
            Intent callServiceIntent = new Intent(context, CallHandlingService.class);
            context.startService(callServiceIntent);
            
            Log.d(TAG, "Services started after boot");
        }
    }
}
