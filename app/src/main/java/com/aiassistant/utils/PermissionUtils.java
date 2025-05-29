package com.aiassistant.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * Utility class for handling permissions and accessibility service settings.
 */
public class PermissionUtils {
    
    private static final String TAG = "PermissionUtils";
    
    /**
     * Check if the specified accessibility service is enabled
     */
    public static boolean isAccessibilityServiceEnabled(Context context, String serviceName) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        
        if (am == null) {
            Log.e(TAG, "AccessibilityManager is null");
            return false;
        }
        
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains(serviceName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Open accessibility settings
     */
    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Check if the app has overlay permission
     */
    public static boolean canDrawOverlays(Context context) {
        return Settings.canDrawOverlays(context);
    }
    
    /**
     * Open overlay settings
     */
    public static void openOverlaySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Check if the app has usage stats permission
     */
    public static boolean hasUsageStatsPermission(Context context) {
        android.app.AppOpsManager appOps = (android.app.AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, 
                android.os.Process.myUid(), context.getPackageName());
        return mode == android.app.AppOpsManager.MODE_ALLOWED;
    }
    
    /**
     * Open usage stats settings
     */
    public static void openUsageStatsSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Check if the app has notification listener permission
     */
    public static boolean isNotificationListenerEnabled(Context context, String serviceName) {
        String enabledListeners = Settings.Secure.getString(
                context.getContentResolver(), "enabled_notification_listeners");
        
        return enabledListeners != null && enabledListeners.contains(serviceName);
    }
    
    /**
     * Open notification listener settings
     */
    public static void openNotificationListenerSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Check if the app has the permission to write secure settings
     */
    public static boolean canWriteSecureSettings(Context context) {
        return context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}