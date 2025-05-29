package com.aiassistant.security;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Blocks and manipulates accessibility events to prevent detection by monitoring services
 * 
 * This class provides methods to detect, block, and manipulate accessibility events
 * to prevent other apps from monitoring the AI Assistant app's activities.
 */
public class AccessibilityEventBlocker {
    private static final String TAG = "AccessibilityEventBlocker";
    
    // Native methods
    private static native boolean blockAccessibilityEventNative(AccessibilityEvent event);
    private static native boolean detectMonitoringServicesNative(Context context);
    private static native boolean spoofAccessibilityEventNative(AccessibilityEvent event, String packageName);
    
    // Load native library
    static {
        try {
            System.loadLibrary("native-lib");
            Log.d(TAG, "Native library loaded for AccessibilityEventBlocker");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load native library for AccessibilityEventBlocker: " + e.getMessage());
        }
    }
    
    private static AccessibilityEventBlocker instance;
    private Context context;
    private AtomicBoolean blockingEnabled = new AtomicBoolean(false);
    private AtomicBoolean monitoringDetected = new AtomicBoolean(false);
    
    /**
     * Get the singleton instance
     * 
     * @return The AccessibilityEventBlocker instance
     */
    public static synchronized AccessibilityEventBlocker getInstance() {
        if (instance == null) {
            instance = new AccessibilityEventBlocker();
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private AccessibilityEventBlocker() {
        // Private constructor for singleton
    }
    
    /**
     * Initialize with context
     * 
     * @param context The application context
     */
    public void initialize(Context context) {
        this.context = context;
        // Start with blocking enabled
        blockingEnabled.set(true);
        // Initial check for monitoring services
        checkForMonitoringServices();
    }
    
    /**
     * Enable or disable event blocking
     * 
     * @param enabled True to enable blocking, false to disable
     */
    public void setBlockingEnabled(boolean enabled) {
        blockingEnabled.set(enabled);
        Log.d(TAG, "Accessibility event blocking " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if blocking is enabled
     * 
     * @return True if blocking is enabled
     */
    public boolean isBlockingEnabled() {
        return blockingEnabled.get();
    }
    
    /**
     * Check if monitoring services are detected
     * 
     * @return True if monitoring services are detected
     */
    public boolean isMonitoringDetected() {
        return monitoringDetected.get();
    }
    
    /**
     * Block an accessibility event if needed
     * 
     * @param event The accessibility event to potentially block
     * @return True if the event should be blocked (not propagated), false otherwise
     */
    public boolean shouldBlockEvent(AccessibilityEvent event) {
        if (!blockingEnabled.get() || event == null) {
            return false;
        }
        
        try {
            // Use native method if available
            return blockAccessibilityEventNative(event);
        } catch (Exception e) {
            Log.e(TAG, "Error in native accessibility event blocking: " + e.getMessage());
            // Fallback to Java implementation
            return shouldBlockEventJava(event);
        }
    }
    
    /**
     * Java implementation of event blocking logic
     * 
     * @param event The accessibility event to check
     * @return True if the event should be blocked
     */
    private boolean shouldBlockEventJava(AccessibilityEvent event) {
        // Package name of the app that triggered the event
        String packageName = "";
        if (event.getPackageName() != null) {
            packageName = event.getPackageName().toString();
        }
        
        // Check if it's our own package
        if (context != null && packageName.equals(context.getPackageName())) {
            // Block all events from our own package
            return true;
        }
        
        // Check event type - block sensitive events
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check for monitoring services
     * 
     * @return True if monitoring services are detected
     */
    public boolean checkForMonitoringServices() {
        if (context == null) {
            return false;
        }
        
        try {
            // Try to use native detection first
            boolean nativeDetected = detectMonitoringServicesNative(context);
            monitoringDetected.set(nativeDetected);
            
            if (nativeDetected) {
                Log.w(TAG, "Monitoring services detected via native method");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in native monitoring detection: " + e.getMessage());
        }
        
        // Fallback to Java implementation
        return checkForMonitoringServicesJava();
    }
    
    /**
     * Java implementation of monitoring service detection
     * 
     * @return True if monitoring services are detected
     */
    private boolean checkForMonitoringServicesJava() {
        if (context == null) {
            return false;
        }
        
        try {
            AccessibilityManager accessibilityManager = 
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            
            if (accessibilityManager != null) {
                List<AccessibilityServiceInfo> enabledServices = 
                    accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
                
                String ourPackage = context.getPackageName();
                
                for (AccessibilityServiceInfo serviceInfo : enabledServices) {
                    String servicePackage = serviceInfo.getResolveInfo().serviceInfo.packageName;
                    
                    // Skip our own service
                    if (servicePackage.equals(ourPackage)) {
                        continue;
                    }
                    
                    // Consider any other service as a potential monitoring service
                    Log.w(TAG, "Potential monitoring service detected: " + servicePackage);
                    monitoringDetected.set(true);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for monitoring services: " + e.getMessage());
        }
        
        monitoringDetected.set(false);
        return false;
    }
    
    /**
     * Spoof an accessibility event
     * 
     * @param event The accessibility event to spoof
     * @param spoofPackage The package name to spoof as
     * @return True if spoofing was successful
     */
    public boolean spoofEvent(AccessibilityEvent event, String spoofPackage) {
        if (event == null || spoofPackage == null) {
            return false;
        }
        
        try {
            // Use native method if available
            return spoofAccessibilityEventNative(event, spoofPackage);
        } catch (Exception e) {
            Log.e(TAG, "Error in native accessibility event spoofing: " + e.getMessage());
            // Currently no Java fallback implementation
            return false;
        }
    }
}
