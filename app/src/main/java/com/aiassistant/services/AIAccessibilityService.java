package com.aiassistant.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.learning.memory.MemoryStorage;

/**
 * Accessibility service for AI assistance
 */
public class AIAccessibilityService extends AccessibilityService {
    private static final String TAG = "AIAccessibilityService";
    
    private AIStateManager aiStateManager;
    private MemoryStorage memoryStorage;
    private PowerManager.WakeLock wakeLock;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AIAccessibilityService created");
        
        // Get managers from AIStateManager
        aiStateManager = AIStateManager.getInstance(this);
        // Note: MemoryStorage doesn't have getMemoryStorage() method
        // We'll just track app usage without the memory component for now
        memoryStorage = null;
        
        // Acquire wake lock to ensure service keeps running
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, 
                "AIAssistant:AccessibilityWakeLock");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
    }
    
    @Override
    public void onDestroy() {
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Process accessibility events for enhanced AI assistance
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            Log.d(TAG, "Window changed: " + packageName);
            
            // Log current app activity (memory storage not yet integrated)
            if (packageName != null && !packageName.isEmpty()) {
                Log.d(TAG, "User switched to app: " + packageName + " at " + System.currentTimeMillis());
            }
        }
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "AIAccessibilityService interrupted");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AIAccessibilityService started");
        return START_STICKY;
    }
}
