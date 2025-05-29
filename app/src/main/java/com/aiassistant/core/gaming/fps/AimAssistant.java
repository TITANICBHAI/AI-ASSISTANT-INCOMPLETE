package com.aiassistant.core.gaming.fps;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced aim assistance system for FPS games.
 * Provides intelligent aim correction based on enemy position, movement patterns, and game context.
 */
public class AimAssistant {
    private static final String TAG = "AimAssistant";
    
    // Aim assists configurations for different games
    private static final Map<String, AimConfig> GAME_CONFIGS = new HashMap<>();
    
    // Default aim assist parameters
    private float assistStrengthSubtle = 0.25f;  // 25% correction
    private float assistStrengthModerate = 0.5f; // 50% correction
    private float assistStrengthHigh = 0.75f;    // 75% correction
    
    // Contextual parameters
    private int screenWidth = 1080;  // Default, will be updated
    private int screenHeight = 2340; // Default, will be updated
    private float maxAimDistance = 300f; // Max distance to apply aim assist
    
    // Aim smoothing parameters
    private boolean useSmoothAiming = true;
    private float aimSmoothingFactor = 0.8f;
    private Point lastAimPoint = null;
    
    // Game-specific aim areas (e.g., head, body)
    private Map<String, Float> bodyPartHeightRatio = new HashMap<>();
    
    // Aim statistics for performance tuning
    private int totalAimAssists = 0;
    private int significantCorrections = 0;
    private float totalCorrectionDistance = 0;
    
    // Context tracking
    private String currentWeaponType = "UNKNOWN";
    private boolean isAiming = false;
    private boolean isFiring = false;
    private boolean isPlayerMoving = false;
    
    static {
        // Initialize game-specific configurations
        
        // PUBG Mobile configuration
        AimConfig pubgConfig = new AimConfig();
        pubgConfig.headOffsetRatio = 0.2f;
        pubgConfig.assistAcceleration = 1.2f;
        pubgConfig.useHeadtrackingPreference = true;
        pubgConfig.maxAssistAngleDegrees = 15.0f;
        pubgConfig.aimSmoothingFactor = 0.85f;
        GAME_CONFIGS.put("com.tencent.ig", pubgConfig);
        
        // Free Fire configuration
        AimConfig freeFireConfig = new AimConfig();
        freeFireConfig.headOffsetRatio = 0.23f;
        freeFireConfig.assistAcceleration = 1.1f;
        freeFireConfig.useHeadtrackingPreference = true;
        freeFireConfig.maxAssistAngleDegrees = 18.0f;
        freeFireConfig.aimSmoothingFactor = 0.8f;
        GAME_CONFIGS.put("com.dts.freefireth", freeFireConfig);
        
        // Call of Duty Mobile configuration
        AimConfig codConfig = new AimConfig();
        codConfig.headOffsetRatio = 0.18f;
        codConfig.assistAcceleration = 1.3f;
        codConfig.useHeadtrackingPreference = true;
        codConfig.maxAssistAngleDegrees = 12.0f;
        codConfig.aimSmoothingFactor = 0.9f;
        GAME_CONFIGS.put("com.activision.callofduty.shooter", codConfig);
    }
    
    // Context for the app
    private final Context context;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AimAssistant(Context context) {
        this.context = context;
        
        // Initialize body part ratios (from top of bounding box)
        bodyPartHeightRatio.put("head", 0.2f);
        bodyPartHeightRatio.put("chest", 0.4f);
        bodyPartHeightRatio.put("waist", 0.6f);
        bodyPartHeightRatio.put("legs", 0.8f);
    }
    
    /**
     * Set screen dimensions for accurate calculations
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     */
    public void setScreenDimensions(int width, int height) {
        if (width > 0 && height > 0) {
            this.screenWidth = width;
            this.screenHeight = height;
            this.maxAimDistance = Math.min(width, height) / 3.0f; // Adjust max aim distance based on screen size
        }
    }
    
    /**
     * Enable or disable smooth aiming
     * @param enabled True to enable, false to disable
     */
    public void setSmoothAimingEnabled(boolean enabled) {
        this.useSmoothAiming = enabled;
    }
    
    /**
     * Set the smoothing factor for aim transitions
     * @param factor Smoothing factor (0.0 to 1.0, higher means smoother)
     */
    public void setAimSmoothingFactor(float factor) {
        if (factor >= 0.0f && factor <= 1.0f) {
            this.aimSmoothingFactor = factor;
        }
    }
    
    /**
     * Set current player context for more intelligent assistance
     * @param weaponType Current weapon type
     * @param isAiming Whether player is in aiming mode
     * @param isFiring Whether player is firing
     * @param isMoving Whether player is moving
     */
    public void setPlayerContext(String weaponType, boolean isAiming, boolean isFiring, boolean isMoving) {
        this.currentWeaponType = weaponType;
        this.isAiming = isAiming;
        this.isFiring = isFiring;
        this.isPlayerMoving = isMoving;
    }
    
    /**
     * Calculate the optimal aim point for a given target
     * @param originalAimPoint Player's original aim point
     * @param targetCenter Center position of the target enemy
     * @param optimalTargetPoint Optimal point to aim at (e.g., head)
     * @param predictedPosition Predicted future position of the enemy
     * @param movementPattern Enemy's identified movement pattern
     * @param assistLevel Assistance level (none, subtle, moderate, significant, auto)
     * @param allEnemyPositions List of all enemy positions for context
     * @return Corrected aim point, or null if unable to calculate
     */
    public Point calculateAimPoint(
            Point originalAimPoint, 
            Point targetCenter, 
            Point optimalTargetPoint,
            Point predictedPosition,
            String movementPattern,
            int assistLevel,
            List<Point> allEnemyPositions) {
        
        // Track statistics
        totalAimAssists++;
        
        // Get assistance strength based on level
        float assistStrength = getAssistStrengthForLevel(assistLevel);
        
        // If no assistance needed, return original point
        if (assistStrength <= 0.0f) {
            return originalAimPoint;
        }
        
        // Choose the best target point (current, optimal, or predicted)
        Point bestTargetPoint = chooseBestTargetPoint(targetCenter, optimalTargetPoint, predictedPosition, movementPattern);
        
        // Calculate distance between original aim and target
        float distance = calculateDistance(originalAimPoint, bestTargetPoint);
        
        // Only apply assistance if within reasonable range
        if (distance > maxAimDistance) {
            return originalAimPoint;
        }
        
        // Calculate assistance vector (direction and magnitude)
        Point assistedPoint = new Point(
                (int)(originalAimPoint.x + (bestTargetPoint.x - originalAimPoint.x) * assistStrength),
                (int)(originalAimPoint.y + (bestTargetPoint.y - originalAimPoint.y) * assistStrength)
        );
        
        // Apply movement adjustments based on pattern
        assistedPoint = adjustForMovementPattern(assistedPoint, movementPattern, bestTargetPoint);
        
        // Apply smoothing if enabled
        if (useSmoothAiming && lastAimPoint != null) {
            // Interpolate between last corrected point and new assisted point
            assistedPoint = new Point(
                    (int)(lastAimPoint.x * aimSmoothingFactor + assistedPoint.x * (1 - aimSmoothingFactor)),
                    (int)(lastAimPoint.y * aimSmoothingFactor + assistedPoint.y * (1 - aimSmoothingFactor))
            );
        }
        
        // Track statistics for significant corrections
        float correctionDistance = calculateDistance(originalAimPoint, assistedPoint);
        if (correctionDistance > 20) {
            significantCorrections++;
        }
        
        totalCorrectionDistance += correctionDistance;
        
        // Update last aim point for next calculation
        lastAimPoint = assistedPoint;
        
        return assistedPoint;
    }
    
    /**
     * Find the best enemy to target and calculate aim assistance
     * @param originalAimPoint Player's original aim point
     * @param enemyPositions List of all enemy positions
     * @param assistLevel Assistance level
     * @return Corrected aim point, or null if no suitable target
     */
    public Point findAndAssistAim(Point originalAimPoint, List<Point> enemyPositions, int assistLevel) {
        if (enemyPositions == null || enemyPositions.isEmpty()) {
            return null;
        }
        
        // Find the closest enemy to current aim
        Point closestEnemy = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Point enemyPoint : enemyPositions) {
            float distance = calculateDistance(originalAimPoint, enemyPoint);
            if (distance < minDistance && distance < maxAimDistance) {
                minDistance = distance;
                closestEnemy = enemyPoint;
            }
        }
        
        if (closestEnemy == null) {
            return originalAimPoint; // No enemies in range
        }
        
        // Calculate optimal target point (head area)
        Point optimalPoint = new Point(
                closestEnemy.x,
                (int)(closestEnemy.y - (minEnemyHeight * bodyPartHeightRatio.get("head")))
        );
        
        // Apply assistance toward this enemy
        return calculateAimPoint(
                originalAimPoint,
                closestEnemy,
                optimalPoint,
                null,
                "UNKNOWN",
                assistLevel,
                enemyPositions
        );
    }
    
    /**
     * Choose the best point to aim at based on context
     */
    private Point chooseBestTargetPoint(Point currentPosition, Point optimalPosition, Point predictedPosition, String movementPattern) {
        // If no optimal position available, use current
        if (optimalPosition == null) {
            return currentPosition;
        }
        
        // If prediction available and enemy is moving in predictable pattern, use prediction
        if (predictedPosition != null && 
            ("STRAFING".equals(movementPattern) || "LINEAR".equals(movementPattern))) {
            
            // For fast weapons, aim ahead. For slow weapons, aim more precisely.
            if ("SNIPER".equals(currentWeaponType) || "MARKSMAN".equals(currentWeaponType)) {
                // For precision weapons, aim directly at optimal position
                return optimalPosition;
            } else {
                // For automatic weapons, lead the target
                return predictedPosition;
            }
        }
        
        // Default to optimal position (usually head area)
        return optimalPosition;
    }
    
    /**
     * Adjust aim point based on enemy movement pattern
     */
    private Point adjustForMovementPattern(Point aimPoint, String movementPattern, Point targetPoint) {
        switch (movementPattern) {
            case "STATIONARY":
                // No adjustment needed for stationary targets
                return aimPoint;
                
            case "ERRATIC":
                // For erratic movements, increase sensitivity to help tracking
                return new Point(
                        (int)(aimPoint.x * 0.9 + targetPoint.x * 0.1),
                        (int)(aimPoint.y * 0.9 + targetPoint.y * 0.1)
                );
                
            case "STRAFING":
                // For strafing (side-to-side) movement, adjust horizontal aim more than vertical
                return new Point(
                        (int)(aimPoint.x * 0.85 + targetPoint.x * 0.15),
                        aimPoint.y
                );
                
            case "VERTICAL":
                // For vertical movement (jumping/falling), adjust vertical aim more
                return new Point(
                        aimPoint.x,
                        (int)(aimPoint.y * 0.85 + targetPoint.y * 0.15)
                );
                
            default:
                return aimPoint;
        }
    }
    
    /**
     * Get assist strength based on the assistance level
     */
    private float getAssistStrengthForLevel(int assistLevel) {
        switch (assistLevel) {
            case FPSGameModule.ASSIST_LEVEL_NONE:
                return 0.0f;
            case FPSGameModule.ASSIST_LEVEL_SUBTLE:
                return assistStrengthSubtle;
            case FPSGameModule.ASSIST_LEVEL_MODERATE:
                return assistStrengthModerate;
            case FPSGameModule.ASSIST_LEVEL_SIGNIFICANT:
                return assistStrengthHigh;
            default:
                return assistStrengthModerate;
        }
    }
    
    /**
     * Calculate distance between two points
     */
    private float calculateDistance(Point p1, Point p2) {
        return (float) Math.sqrt(
                Math.pow(p2.x - p1.x, 2) + 
                Math.pow(p2.y - p1.y, 2)
        );
    }
    
    /**
     * Reset smoothing to avoid aim drift when new targets appear
     */
    public void resetSmoothing() {
        lastAimPoint = null;
    }
    
    /**
     * Get the percentage of significant corrections made
     * @return Percentage of aim assists that were significant
     */
    public float getSignificantCorrectionPercentage() {
        if (totalAimAssists == 0) {
            return 0;
        }
        return (float) significantCorrections / totalAimAssists;
    }
    
    /**
     * Get average correction distance
     * @return Average distance in pixels
     */
    public float getAverageCorrectionDistance() {
        if (totalAimAssists == 0) {
            return 0;
        }
        return totalCorrectionDistance / totalAimAssists;
    }
    
    // Estimated minimum enemy height for head position calculation
    private static int minEnemyHeight = 120;
    
    /**
     * Set the expected minimum enemy height
     * @param height Height in pixels
     */
    public void setMinEnemyHeight(int height) {
        if (height > 0) {
            minEnemyHeight = height;
        }
    }
    
    /**
     * Configuration class for game-specific aim assistance parameters
     */
    private static class AimConfig {
        // Vertical ratio for head position from top of bounding box
        public float headOffsetRatio = 0.2f;
        
        // Acceleration factor for aim movement
        public float assistAcceleration = 1.0f;
        
        // Whether to prioritize head tracking
        public boolean useHeadtrackingPreference = true;
        
        // Maximum assist angle in degrees
        public float maxAssistAngleDegrees = 15.0f;
        
        // Aim smoothing factor (0-1)
        public float aimSmoothingFactor = 0.8f;
    }
}
