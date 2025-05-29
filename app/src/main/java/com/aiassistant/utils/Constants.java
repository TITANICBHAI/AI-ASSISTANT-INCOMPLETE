package com.aiassistant.utils;

/**
 * Application constants
 */
public class Constants {
    // Database
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "aiassistant_db";
    
    // Feature extraction
    public static final int FEATURE_VECTOR_SIZE = 128;
    
    // ML model settings
    public static final int BATCH_SIZE = 64;
    public static final float LEARNING_RATE = 0.001f;
    public static final float DISCOUNT_FACTOR = 0.99f;
    
    // Security settings
    public static final int SECURITY_CHECK_INTERVAL_MS = 5000;
    public static final int MAX_FAILED_AUTH_ATTEMPTS = 5;
    
    // Performance monitoring
    public static final long PERFORMANCE_LOG_INTERVAL_MS = 60000;
    
    // App settings
    public static final String PREF_KEY_FIRST_RUN = "first_run";
    public static final String PREF_KEY_VOICE_ENABLED = "voice_enabled";
    public static final String PREF_KEY_ANALYTICS_ENABLED = "analytics_enabled";
    
    // Voice processing
    public static final int SAMPLE_RATE = 16000;
    public static final int RECORDING_DURATION_MS = 5000;
    
    // Neural network settings
    public static final int HIDDEN_LAYER_SIZE = 256;
    public static final String MODEL_FILE_NAME = "ai_model.tflite";
    
    // AI Assistant settings
    public static final int MAX_CONTEXT_HISTORY = 10;
    public static final int MAX_MEMORY_ENTRIES = 1000;
    
    // Privacy settings
    public static final boolean DEFAULT_ANALYTICS_ENABLED = false;
    public static final boolean DEFAULT_VOICE_ENABLED = true;
    
    // Task scheduling
    public static final long MIN_TASK_INTERVAL_MS = 1000;
    public static final int MAX_CONCURRENT_TASKS = 5;
    
    // Security
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int ENCRYPTION_KEY_SIZE = 256;
    
    // Privacy
    public static final long DATA_RETENTION_DAYS = 30;
    
    // Feature flags
    public static final boolean ENABLE_ADVANCED_VOICE = true;
    public static final boolean ENABLE_DEEP_LEARNING = true;
    public static final boolean ENABLE_EMOTION_DETECTION = true;
    public static final boolean ENABLE_GAME_PROFILES = true;
    
    // API endpoints
    public static final String API_BASE_URL = "https://api.example.com/v1/";
    
    private Constants() {
        // Prevent instantiation
    }
}
