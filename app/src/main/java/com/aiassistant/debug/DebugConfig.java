package com.aiassistant.debug;

/**
 * Debug configuration settings
 */
public class DebugConfig {
    // Master debug switch
    public static final boolean DEBUG_MODE = true;
    
    // Feature-specific debug flags
    public static final boolean DEBUG_SECURITY = true;
    public static final boolean DEBUG_VOICE = true;
    public static final boolean DEBUG_GAME_ANALYSIS = true;
    public static final boolean DEBUG_EMOTIONAL_INTELLIGENCE = true;
    public static final boolean DEBUG_MULTILINGUAL = true;
    
    // Performance monitoring
    public static final boolean MONITOR_PERFORMANCE = true;
    public static final boolean LOG_MEMORY_USAGE = true;
    
    // Debug visualization
    public static final boolean SHOW_DEBUG_OVERLAY = true;
    public static final boolean VISUALIZE_TACTICAL_DATA = true;
    
    static {
        // Initialize debug logger
        DebugLogger.setDebugEnabled(DEBUG_MODE);
    }
}
