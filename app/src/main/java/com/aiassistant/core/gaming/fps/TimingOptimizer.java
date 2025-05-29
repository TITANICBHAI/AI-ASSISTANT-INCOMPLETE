package com.aiassistant.core.gaming.fps;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced timing optimization system for FPS games.
 * Calculates optimal timing for game actions such as shooting, reloading, and ability usage
 * based on enemy movement patterns and game context.
 */
public class TimingOptimizer {
    private static final String TAG = "TimingOptimizer";
    
    // Game-specific configuration mappings
    private static final Map<String, TimingConfig> GAME_CONFIGS = new HashMap<>();
    
    // Action types for reference
    private static final int ACTION_SHOOT = FPSGameModule.ACTION_SHOOT;
    private static final int ACTION_RELOAD = FPSGameModule.ACTION_RELOAD;
    private static final int ACTION_JUMP = FPSGameModule.ACTION_JUMP;
    private static final int ACTION_CROUCH = FPSGameModule.ACTION_CROUCH;
    private static final int ACTION_ABILITY = FPSGameModule.ACTION_ABILITY;
    
    // Assist level constants
    private static final int ASSIST_LEVEL_NONE = FPSGameModule.ASSIST_LEVEL_NONE;
    private static final int ASSIST_LEVEL_SUBTLE = FPSGameModule.ASSIST_LEVEL_SUBTLE;
    private static final int ASSIST_LEVEL_MODERATE = FPSGameModule.ASSIST_LEVEL_MODERATE;
    private static final int ASSIST_LEVEL_SIGNIFICANT = FPSGameModule.ASSIST_LEVEL_SIGNIFICANT;
    
    // Current game context
    private String currentWeaponType = "UNKNOWN";
    private float currentPingMs = 50.0f;
    private boolean isInAimMode = false;
    
    // Performance tracking
    private int totalTimingOptimizations = 0;
    private int successfulTimingOptimizations = 0;
    private float lastFrameTime = 0;
    
    // Context
    private final Context context;
    
    static {
        // Initialize game-specific configurations
        
        // PUBG Mobile configuration
        TimingConfig pubgConfig = new TimingConfig();
        pubgConfig.frameTimeMs = 16.67f; // ~60 FPS
        pubgConfig.defaultShotDelay = 50;
        pubgConfig.defaultReloadDelay = 200;
        pubgConfig.movementPredictionTime = 150;
        GAME_CONFIGS.put("com.tencent.ig", pubgConfig);
        
        // Free Fire configuration
        TimingConfig freeFireConfig = new TimingConfig();
        freeFireConfig.frameTimeMs = 16.67f; // ~60 FPS
        freeFireConfig.defaultShotDelay = 40;
        freeFireConfig.defaultReloadDelay = 180;
        freeFireConfig.movementPredictionTime = 120;
        GAME_CONFIGS.put("com.dts.freefireth", freeFireConfig);
        
        // Call of Duty Mobile configuration
        TimingConfig codConfig = new TimingConfig();
        codConfig.frameTimeMs = 16.67f; // ~60 FPS
        codConfig.defaultShotDelay = 45;
        codConfig.defaultReloadDelay = 220;
        codConfig.movementPredictionTime = 140;
        GAME_CONFIGS.put("com.activision.callofduty.shooter", codConfig);
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public TimingOptimizer(Context context) {
        this.context = context;
    }
    
    /**
     * Set current game context for better timing optimization
     * @param weaponType Current weapon type ("ASSAULT", "SNIPER", etc.)
     * @param pingMs Current ping in milliseconds
     * @param inAimMode Whether player is in aiming mode
     */
    public void setGameContext(String weaponType, float pingMs, boolean inAimMode) {
        this.currentWeaponType = weaponType;
        this.currentPingMs = pingMs;
        this.isInAimMode = inAimMode;
    }
    
    /**
     * Set the current frame time in milliseconds
     * @param frameTimeMs Average frame time
     */
    public void setFrameTime(float frameTimeMs) {
        if (frameTimeMs > 0) {
            this.lastFrameTime = frameTimeMs;
        }
    }
    
    /**
     * Calculate the optimal timing for a game action
     * @param actionType Type of action (SHOOT, RELOAD, JUMP, etc.)
     * @param targetPosition Position of the target
     * @param movementPattern Detected movement pattern of the target
     * @param targetSpeed Speed of the target in pixels per second
     * @param assistLevel Timing assistance level
     * @return Optimal delay in milliseconds, or -1 if calculation not possible
     */
    public long calculateOptimalTiming(
            int actionType, 
            Point targetPosition, 
            String movementPattern,
            float targetSpeed,
            int assistLevel) {
        
        // Track optimization attempts
        totalTimingOptimizations++;
        
        // If assistance is disabled, return -1 to indicate no optimization
        if (assistLevel == ASSIST_LEVEL_NONE) {
            return -1;
        }
        
        TimingConfig config = new TimingConfig(); // Default config
        
        // Calculate base timing based on action type
        long baseTiming = calculateBaseTimingForAction(actionType, config);
        
        // Apply movement pattern adjustments
        long patternAdjustment = calculateMovementPatternAdjustment(movementPattern, targetSpeed, config);
        
        // Apply network latency compensation
        long latencyAdjustment = calculateLatencyAdjustment(config);
        
        // Apply weapon-specific adjustments
        long weaponAdjustment = calculateWeaponAdjustment(config);
        
        // Apply frame timing adjustments (for frame-perfect actions)
        long frameAdjustment = calculateFrameTimingAdjustment(config);
        
        // Calculate total timing
        long optimalTiming = baseTiming + patternAdjustment + latencyAdjustment + 
                           weaponAdjustment + frameAdjustment;
        
        // Clamp to reasonable range
        optimalTiming = Math.max(0, Math.min(500, optimalTiming));
        
        // Apply assistance level - reduce timing for higher assistance levels
        float assistanceFactor = getAssistanceFactor(assistLevel);
        optimalTiming = Math.round(optimalTiming * assistanceFactor);
        
        // Track successful optimizations
        successfulTimingOptimizations++;
        
        // Log the calculated timing for debugging
        Log.d(TAG, String.format("Calculated timing: action=%d, pattern=%s, base=%d, final=%d",
                actionType, movementPattern, baseTiming, optimalTiming));
        
        return optimalTiming;
    }
    
    /**
     * Calculate base timing for an action
     */
    private long calculateBaseTimingForAction(int actionType, TimingConfig config) {
        switch (actionType) {
            case ACTION_SHOOT:
                return config.defaultShotDelay;
                
            case ACTION_RELOAD:
                return config.defaultReloadDelay;
                
            case ACTION_JUMP:
                return 100; // Default jump timing
                
            case ACTION_CROUCH:
                return 80;  // Default crouch timing
                
            case ACTION_ABILITY:
                return 150; // Default ability timing
                
            default:
                return 100; // Default timing
        }
    }
    
    /**
     * Calculate timing adjustment based on enemy movement pattern
     */
    private long calculateMovementPatternAdjustment(String movementPattern, float targetSpeed, TimingConfig config) {
        if (movementPattern == null) {
            return 0;
        }
        
        switch (movementPattern) {
            case CombatPatternRecognizer.PATTERN_STATIONARY:
                // No adjustment needed for stationary targets
                return -config.defaultShotDelay / 2; // Reduce delay for easy targets
                
            case CombatPatternRecognizer.PATTERN_STRAFING:
                // For strafing targets, timing depends on the direction change frequency
                return Math.round(config.movementPredictionTime * 0.8f);
                
            case CombatPatternRecognizer.PATTERN_ERRATIC:
                // For erratic movement, better to fire quickly
                return Math.round(config.movementPredictionTime * 0.3f);
                
            case CombatPatternRecognizer.PATTERN_LINEAR:
                // For linear movement, can predict fairly accurately
                return Math.round(targetSpeed * 0.1f); // Proportional to speed
                
            case CombatPatternRecognizer.PATTERN_VERTICAL:
                // For vertical movement, timing depends on jump/fall mechanics
                return Math.round(config.movementPredictionTime * 0.6f);
                
            case CombatPatternRecognizer.PATTERN_CIRCLING:
                // For circling movement, lead the target
                return Math.round(config.movementPredictionTime * 0.7f);
                
            case CombatPatternRecognizer.PATTERN_PEEKING:
                // For peeking targets, time for when they re-peek
                return Math.round(config.movementPredictionTime * 1.2f);
                
            default:
                return 0; // No adjustment for unknown patterns
        }
    }
    
    /**
     * Calculate timing adjustment for network latency
     */
    private long calculateLatencyAdjustment(TimingConfig config) {
        // Basic latency compensation - adjust timing based on ping
        // Note: We reduce timing for higher ping, as the action may already be delayed
        if (currentPingMs > 100) {
            // For high ping, reduce timing to compensate for additional network delay
            return -Math.round(Math.min(100, (currentPingMs - 100) / 2));
        } else {
            // For low ping, no adjustment needed
            return 0;
        }
    }
    
    /**
     * Calculate timing adjustment based on weapon type
     */
    private long calculateWeaponAdjustment(TimingConfig config) {
        if (currentWeaponType == null) {
            return 0;
        }
        
        switch (currentWeaponType) {
            case "SNIPER":
                // Sniper rifles need more precise timing
                return Math.round(config.movementPredictionTime * 0.5f);
                
            case "SHOTGUN":
                // Shotguns can be more forgiving due to spread
                return Math.round(-config.movementPredictionTime * 0.3f);
                
            case "ASSAULT":
                // Assault rifles have moderate timing requirements
                return 0;
                
            case "SMG":
                // SMGs fire rapidly, so timing is less critical
                return -20;
                
            default:
                return 0;
        }
    }
    
    /**
     * Calculate frame timing adjustment for frame-perfect actions
     */
    private long calculateFrameTimingAdjustment(TimingConfig config) {
        // If we have frame time information, adjust timing to align with frame boundaries
        if (lastFrameTime > 0) {
            // Round to nearest frame
            float frameTime = (lastFrameTime > 0) ? lastFrameTime : config.frameTimeMs;
            return Math.round(frameTime - (config.defaultShotDelay % frameTime));
        }
        return 0;
    }
    
    /**
     * Get assistance factor based on assistance level
     */
    private float getAssistanceFactor(int assistLevel) {
        switch (assistLevel) {
            case ASSIST_LEVEL_SUBTLE:
                return 0.9f; // 10% assistance
            case ASSIST_LEVEL_MODERATE:
                return 0.75f; // 25% assistance
            case ASSIST_LEVEL_SIGNIFICANT:
                return 0.5f; // 50% assistance
            default:
                return 1.0f; // No assistance
        }
    }
    
    /**
     * Calculate optimal timing for a sequence of shots (burst fire)
     * @param actionType Type of action (typically SHOOT)
     * @param shotsInBurst Number of shots in the burst
     * @param movementPattern Enemy movement pattern
     * @param assistLevel Assistance level
     * @return Array of optimal delay times for each shot in milliseconds
     */
    public long[] calculateBurstTiming(
            int actionType, 
            int shotsInBurst, 
            String movementPattern, 
            int assistLevel) {
        
        if (shotsInBurst <= 0) {
            return new long[0];
        }
        
        long[] timings = new long[shotsInBurst];
        
        // Base timing for first shot
        timings[0] = calculateOptimalTiming(
                actionType, null, movementPattern, 0, assistLevel);
        
        // For subsequent shots, reduce timing progressively
        for (int i = 1; i < shotsInBurst; i++) {
            // Each subsequent shot needs less delay as the player is already tracking
            float reductionFactor = 1.0f - (i / (float)shotsInBurst) * 0.5f;
            timings[i] = Math.round(timings[0] * reductionFactor);
        }
        
        return timings;
    }
    
    /**
     * Get the success rate of timing optimizations
     * @return Percentage of successful optimizations
     */
    public float getOptimizationSuccessRate() {
        if (totalTimingOptimizations == 0) {
            return 0;
        }
        return (float) successfulTimingOptimizations / totalTimingOptimizations;
    }
    
    /**
     * Configuration class for game-specific timing parameters
     */
    private static class TimingConfig {
        // Frame time in milliseconds (e.g., 16.67ms for 60 FPS)
        public float frameTimeMs = 16.67f;
        
        // Default delay for shooting actions in milliseconds
        public long defaultShotDelay = 50;
        
        // Default delay for reload actions in milliseconds
        public long defaultReloadDelay = 200;
        
        // Time to predict movement ahead in milliseconds
        public float movementPredictionTime = 150;
    }
}
