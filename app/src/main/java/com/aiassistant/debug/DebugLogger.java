package com.aiassistant.debug;

import android.util.Log;

/**
 * Debug logging utility that can be enabled/disabled
 * and provides different levels of logging.
 */
public class DebugLogger {
    private static final String TAG = "AIAssistant";
    private static boolean DEBUG_ENABLED = false;
    
    /**
     * Enable or disable debug logging
     * @param enabled true to enable
     */
    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
    }
    
    /**
     * Check if debug is enabled
     * @return true if enabled
     */
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }
    
    /**
     * Log debug message
     * @param tag Log tag
     * @param message Message to log
     */
    public static void d(String tag, String message) {
        if (DEBUG_ENABLED) {
            Log.d(tag, message);
        }
    }
    
    /**
     * Log info message
     * @param tag Log tag
     * @param message Message to log
     */
    public static void i(String tag, String message) {
        if (DEBUG_ENABLED) {
            Log.i(tag, message);
        }
    }
    
    /**
     * Log warning message
     * @param tag Log tag
     * @param message Message to log
     */
    public static void w(String tag, String message) {
        if (DEBUG_ENABLED) {
            Log.w(tag, message);
        }
    }
    
    /**
     * Log error message
     * @param tag Log tag
     * @param message Message to log
     */
    public static void e(String tag, String message) {
        // Always log errors
        Log.e(tag, message);
    }
    
    /**
     * Log error message with exception
     * @param tag Log tag
     * @param message Message to log
     * @param e Exception
     */
    public static void e(String tag, String message, Throwable e) {
        // Always log errors
        Log.e(tag, message, e);
    }
}
