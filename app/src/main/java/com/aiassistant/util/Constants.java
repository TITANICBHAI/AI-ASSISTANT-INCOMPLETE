package com.aiassistant.util;

/**
 * Deprecated: Please use com.aiassistant.utils.Constants instead.
 * This class forwards all constant references to the main Constants class in the utils package.
 */
@Deprecated
public class Constants {
    // Forward all constants from utils package
    
    // AI Modes
    public static final int AI_MODE_DISABLED = com.aiassistant.utils.Constants.AI_MODE_DISABLED;
    public static final int AI_MODE_AUTO = com.aiassistant.utils.Constants.AI_MODE_AUTO;
    public static final int AI_MODE_COPILOT = com.aiassistant.utils.Constants.AI_MODE_COPILOT;
    public static final int AI_MODE_LEARNING = com.aiassistant.utils.Constants.AI_MODE_LEARNING;
    
    // AI Performance Profiles
    public static final int AI_PERFORMANCE_BALANCED = com.aiassistant.utils.Constants.AI_PERFORMANCE_BALANCED;
    public static final int AI_PERFORMANCE_ACCURACY = com.aiassistant.utils.Constants.AI_PERFORMANCE_ACCURACY;
    public static final int AI_PERFORMANCE_SPEED = com.aiassistant.utils.Constants.AI_PERFORMANCE_SPEED;
    
    // Learning Parameters
    public static final int MIN_TRAINING_SAMPLES = com.aiassistant.utils.Constants.MIN_TRAINING_SAMPLES;
    public static final int RECOMMENDED_TRAINING_SAMPLES = com.aiassistant.utils.Constants.RECOMMENDED_TRAINING_SAMPLES;
    public static final int MAX_FRAME_RATE = com.aiassistant.utils.Constants.MAX_FRAME_RATE;
    public static final int FRAME_DOWNSCALE_FACTOR = com.aiassistant.utils.Constants.FRAME_DOWNSCALE_FACTOR;
    
    // AI Algorithm Parameters
    public static final float DEFAULT_LEARNING_RATE = com.aiassistant.utils.Constants.DEFAULT_LEARNING_RATE;
    public static final float DEFAULT_DISCOUNT_FACTOR = com.aiassistant.utils.Constants.DEFAULT_DISCOUNT_FACTOR;
    public static final float DEFAULT_EXPLORATION_RATE = com.aiassistant.utils.Constants.DEFAULT_EXPLORATION_RATE;
    public static final int DEFAULT_BATCH_SIZE = com.aiassistant.utils.Constants.DEFAULT_BATCH_SIZE;
    
    // Meta-Learning Parameters
    public static final int META_ADAPTATION_ITERATIONS = com.aiassistant.utils.Constants.META_ADAPTATION_ITERATIONS;
    public static final float META_LEARNING_RATE = com.aiassistant.utils.Constants.META_LEARNING_RATE;
    public static final float META_GRADIENT_CLIP = com.aiassistant.utils.Constants.META_GRADIENT_CLIP;
    public static final int META_UPDATE_INTERVAL = com.aiassistant.utils.Constants.META_UPDATE_INTERVAL;
    public static final float TRANSFER_LEARNING_WEIGHT = com.aiassistant.utils.Constants.TRANSFER_LEARNING_WEIGHT;
    
    // Game Detection
    public static final long GAME_DETECTION_INTERVAL_MS = com.aiassistant.utils.Constants.GAME_DETECTION_INTERVAL_MS_LONG;
    public static final int MIN_DETECTION_CONFIDENCE = com.aiassistant.utils.Constants.MIN_DETECTION_CONFIDENCE;
    
    // File Paths
    public static final String MODELS_DIR = com.aiassistant.utils.Constants.MODELS_DIR;
    public static final String TRAINING_DATA_DIR = com.aiassistant.utils.Constants.TRAINING_DATA_DIR;
    public static final String CACHE_DIR = com.aiassistant.utils.Constants.CACHE_DIR;
    
    // Shared Preferences
    public static final String PREFS_NAME = com.aiassistant.utils.Constants.PREF_FILE_NAME;
    public static final String PREF_FILE_NAME = com.aiassistant.utils.Constants.PREF_FILE_NAME;
    public static final String PREF_AI_MODE = com.aiassistant.utils.Constants.PREF_AI_MODE;
    public static final String PREF_PERFORMANCE_PROFILE = com.aiassistant.utils.Constants.PREF_PERFORMANCE_PROFILE;
    public static final String PREF_LAST_GAME = com.aiassistant.utils.Constants.PREF_LAST_GAME;
    public static final String PREF_TRAINING_ENABLED = com.aiassistant.utils.Constants.PREF_TRAINING_ENABLED;
    public static final String PREF_FRAME_RATE = com.aiassistant.utils.Constants.PREF_FRAME_RATE;
    public static final String PREF_VIDEO_QUALITY = com.aiassistant.utils.Constants.PREF_VIDEO_QUALITY;
    public static final int DEFAULT_FRAME_RATE = com.aiassistant.utils.Constants.DEFAULT_FRAME_RATE;
    public static final int DEFAULT_VIDEO_QUALITY = com.aiassistant.utils.Constants.DEFAULT_VIDEO_QUALITY;
    
    // Optimized Games
    public static final String GAME_PUBG = com.aiassistant.utils.Constants.GAME_PUBG;
    public static final String GAME_FREEFIRE = com.aiassistant.utils.Constants.GAME_FREEFIRE;
    public static final String GAME_COD = com.aiassistant.utils.Constants.GAME_COD;
    
    // Actions
    public static final int ACTION_NONE = com.aiassistant.utils.Constants.ACTION_NONE;
    public static final int ACTION_TAP = com.aiassistant.utils.Constants.ACTION_TAP;
    public static final int ACTION_SWIPE = com.aiassistant.utils.Constants.ACTION_SWIPE;
    public static final int ACTION_HOLD = com.aiassistant.utils.Constants.ACTION_HOLD;
    
    // Service Actions
    public static final String ACTION_START_AI = com.aiassistant.utils.Constants.ACTION_START_AI;
    public static final String ACTION_STOP_AI = com.aiassistant.utils.Constants.ACTION_STOP_AI;
    public static final String ACTION_PAUSE_AI = com.aiassistant.utils.Constants.ACTION_PAUSE_AI;
    public static final String ACTION_CHANGE_MODE = com.aiassistant.utils.Constants.ACTION_CHANGE_MODE;
    
    // Notification IDs
    public static final int NOTIFICATION_SERVICE_ID = com.aiassistant.utils.Constants.NOTIFICATION_SERVICE_ID;
    public static final int NOTIFICATION_LEARNING_ID = com.aiassistant.utils.Constants.NOTIFICATION_LEARNING_ID;
    
    // Permissions
    public static final int REQUEST_OVERLAY_PERMISSION = com.aiassistant.utils.Constants.REQUEST_OVERLAY_PERMISSION;
    public static final int REQUEST_ACCESSIBILITY_PERMISSION = com.aiassistant.utils.Constants.REQUEST_ACCESSIBILITY_PERMISSION;
    public static final int REQUEST_STORAGE_PERMISSION = com.aiassistant.utils.Constants.REQUEST_STORAGE_PERMISSION;
    
    // Timeouts
    public static final int DETECTION_TIMEOUT_MS = com.aiassistant.utils.Constants.DETECTION_TIMEOUT_MS;
    public static final int CONNECTION_TIMEOUT_MS = com.aiassistant.utils.Constants.CONNECTION_TIMEOUT_MS;
    
    // Database
    public static final int DATABASE_VERSION = com.aiassistant.utils.Constants.DATABASE_VERSION;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}
