package com.aiassistant.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Lightweight accessibility service for detecting and monitoring applications
 * This service only observes changes in the UI without performing any actions
 * Used to detect when games are launched and monitor UI state changes
 */
public class AccessibilityDetectionService extends AccessibilityService {
    private static final String TAG = "AccessDetectionService";
    
    // Static instance for singleton access
    private static AccessibilityDetectionService instance;
    
    // Current app being observed
    private String currentPackage;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Accessibility Detection Service created");
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        
        // Configure service capabilities
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            info = new AccessibilityServiceInfo();
        }
        
        // Set capabilities - minimal for detection only
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        
        // Apply configuration
        setServiceInfo(info);
        
        Log.d(TAG, "Accessibility Detection Service connected");
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        
        // Track current package
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String newPackage = event.getPackageName().toString();
                
                // Check for package change
                if (!newPackage.equals(currentPackage)) {
                    currentPackage = newPackage;
                    Log.d(TAG, "Current app changed: " + currentPackage);
                    
                    // Check if this is a game and notify GameInteractionService
                    if (isGamePackage(currentPackage)) {
                        notifyGameDetected(currentPackage);
                    }
                }
            }
        }
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Detection Service interrupted");
    }
    
    @Override
    public void onDestroy() {
        if (instance == this) {
            instance = null;
        }
        super.onDestroy();
        Log.d(TAG, "Accessibility Detection Service destroyed");
    }
    
    /**
     * Get static instance of the service
     */
    public static AccessibilityDetectionService getInstance() {
        return instance;
    }
    
    /**
     * Get the current package name
     */
    public String getCurrentPackage() {
        return currentPackage;
    }
    
    /**
     * Check if the current package is a game
     */
    private boolean isGamePackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        
        // Known game packages
        return packageName.contains("com.dts.freefireth") || 
               packageName.contains("com.tencent.ig") ||
               packageName.contains("com.pubg.imobile") ||
               packageName.contains("com.activision.callofduty") ||
               packageName.contains("com.tencent.tmgp");
    }
    
    /**
     * Notify that a game was detected
     */
    private void notifyGameDetected(String packageName) {
        Log.d(TAG, "Game detected: " + packageName);
        
        // Notify via broadcast
        Intent intent = new Intent("com.aiassistant.GAME_DETECTED");
        intent.putExtra("package_name", packageName);
        sendBroadcast(intent);
    }
}
