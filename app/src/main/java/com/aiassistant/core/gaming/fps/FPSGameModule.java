package com.aiassistant.core.gaming.fps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.aiassistant.core.ai.perception.VisualPerceptionManager;
import com.aiassistant.core.gaming.GameModule;
import com.aiassistant.core.gaming.fps.AimAssistant;
import com.aiassistant.core.gaming.fps.CombatPatternRecognizer;
import com.aiassistant.core.gaming.fps.EnemyDetector;
import com.aiassistant.core.gaming.fps.TimingOptimizer;
import com.aiassistant.utils.BitmapUtils;
import com.aiassistant.utils.Constants;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Game module specialized for FPS (First Person Shooter) games.
 * Contains algorithms for enemy detection, aim assistance, and tactical analysis.
 */
public class FPSGameModule extends GameModule {
    private static final String TAG = "FPSGameModule";
    
    // Action types for frame-perfect timing
    public static final int ACTION_SHOOT = 0;
    public static final int ACTION_RELOAD = 1;
    public static final int ACTION_JUMP = 2;
    public static final int ACTION_CROUCH = 3;
    public static final int ACTION_ABILITY = 4;
    
    // Assist levels for adaptive assistance
    public static final int ASSIST_LEVEL_NONE = 0;         // No assistance
    public static final int ASSIST_LEVEL_SUBTLE = 1;       // Subtle assistance (minimal)
    public static final int ASSIST_LEVEL_MODERATE = 2;     // Moderate assistance (balanced)
    public static final int ASSIST_LEVEL_SIGNIFICANT = 3;  // Significant assistance (strong)
    public static final int ASSIST_LEVEL_AUTO = 4;         // Auto (AI determines level based on performance)
    
    // Module configuration
    private boolean enhancedEnemyDetection = true;
    private boolean useContourAnalysis = true;
    private boolean useColorDetection = true;
    private boolean useMovementDetection = true;
    private boolean enableAimAssistance = true;
    private boolean enableCombatPatternRecognition = true;
    
    // Detection thresholds
    private float enemyConfidenceThreshold = 0.65f;
    private float movementThreshold = 5.0f;
    private int minEnemySize = 30;
    
    // Previous frame for movement detection
    private Bitmap previousFrame = null;
    
    // OpenCV status
    private boolean openCvInitialized = false;
    
    // Enemy tracking system for pattern recognition
    private final Map<String, EnemyModel> trackedEnemies = new HashMap<>();
    private long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL_MS = 5000; // Clean up stale enemies every 5 seconds
    private static final long ENEMY_TIMEOUT_MS = 3000;    // Consider enemy lost after 3 seconds
    
    // Adaptive assistance settings
    private int aimAssistLevel = ASSIST_LEVEL_MODERATE;
    private int timingAssistLevel = ASSIST_LEVEL_MODERATE;
    private int movementAssistLevel = ASSIST_LEVEL_MODERATE;
    private int tacticalAssistLevel = ASSIST_LEVEL_MODERATE;
    
    // Performance metrics for auto-adjustment
    private int hitCount = 0;
    private int missCount = 0;
    private int deathCount = 0;
    private int killCount = 0;
    private long lastPerformanceAdjustment = 0;
    private static final long PERFORMANCE_ADJUSTMENT_INTERVAL_MS = 60000; // Adjust every minute
    
    // Advanced modules for enhanced AI capabilities
    private VisualPerceptionManager visualPerceptionManager;
    private EnemyDetector enemyDetector;
    private CombatPatternRecognizer combatPatternRecognizer;
    private AimAssistant aimAssistant;
    private TimingOptimizer timingOptimizer;
    
    // Current game package for game-specific optimizations
    private String currentGamePackage = null;
    
    /**
     * Constructor
     * @param context Application context
     */
    public FPSGameModule(Context context) {
        super(context);
        
        // Initialize OpenCV for advanced image processing
        initOpenCV();
        
        // Initialize AI assistance modules
        visualPerceptionManager = new VisualPerceptionManager(context);
        enemyDetector = new EnemyDetector(context, visualPerceptionManager);
        combatPatternRecognizer = new CombatPatternRecognizer(context);
        aimAssistant = new AimAssistant(context);
        timingOptimizer = new TimingOptimizer(context);
        
        // Set default detection thresholds
        enemyDetector.setDetectionThreshold(enemyConfidenceThreshold);
    }
    
    /**
     * Initialize OpenCV
     */
    private void initOpenCV() {
        try {
            openCvInitialized = OpenCVLoader.initDebug();
            if (openCvInitialized) {
                Log.d(TAG, "OpenCV initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize OpenCV");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OpenCV", e);
            openCvInitialized = false;
        }
    }
    
    @Override
    public void onScreenUpdated(Bitmap screenshot) {
        super.onScreenUpdated(screenshot);
        
        // Process the screenshot for FPS-specific features
        if (screenshot != null) {
            // Detect enemies in the frame
            List<Rect> enemies = detectEnemies(screenshot);
            
            // Update game state with enemy information
            if (enemies != null && !enemies.isEmpty()) {
                Log.d(TAG, "Detected " + enemies.size() + " potential enemies");
                
                // Update enemy tracking for combat pattern recognition
                if (enableCombatPatternRecognition) {
                    updateTrackedEnemies(enemies);
                }
                
                // Store enemy locations in game state
                for (Rect enemy : enemies) {
                    Point center = new Point(
                            enemy.left + enemy.width() / 2,
                            enemy.top + enemy.height() / 2
                    );
                    addEnemyToGameState(center, enemy);
                }
            }
            
            // Clean up stale enemy tracking
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
                cleanupStaleEnemies();
                lastCleanupTime = currentTime;
            }
            
            // Update previous frame for next iteration
            if (previousFrame != null && previousFrame != screenshot) {
                previousFrame.recycle();
            }
            previousFrame = screenshot.copy(screenshot.getConfig(), true);
        }
    }
    
    /**
     * Update tracked enemies with new detections
     * Used for combat pattern recognition
     * @param detectedEnemies List of enemy bounding boxes
     */
    private void updateTrackedEnemies(List<Rect> detectedEnemies) {
        // Mark all current enemies as not updated in this frame
        Map<String, Boolean> updated = new HashMap<>();
        for (String id : trackedEnemies.keySet()) {
            updated.put(id, false);
        }
        
        // Try to match detected enemies with existing tracks
        for (Rect enemyRect : detectedEnemies) {
            boolean matched = false;
            
            // Calculate center point
            Point center = new Point(
                    enemyRect.left + enemyRect.width() / 2,
                    enemyRect.top + enemyRect.height() / 2
            );
            
            // Try to find a match in existing tracked enemies
            for (Map.Entry<String, EnemyModel> entry : trackedEnemies.entrySet()) {
                EnemyModel model = entry.getValue();
                
                // Check if the detected enemy is close to this tracked enemy
                Point trackedCenter = model.getCenter();
                float distance = calculateDistance(center, trackedCenter);
                
                // If close enough, update the tracked enemy
                if (distance < Math.max(enemyRect.width(), enemyRect.height())) {
                    model.update(enemyRect);
                    updated.put(entry.getKey(), true);
                    matched = true;
                    break;
                }
            }
            
            // If no match found, create a new tracked enemy
            if (!matched) {
                String newId = "enemy_" + System.currentTimeMillis() + "_" + trackedEnemies.size();
                EnemyModel newEnemy = new EnemyModel(newId, enemyRect);
                trackedEnemies.put(newId, newEnemy);
                updated.put(newId, true);
                
                Log.d(TAG, "New enemy tracked: " + newId);
            }
        }
        
        // Report on recognized combat patterns
        for (EnemyModel enemy : trackedEnemies.values()) {
            if (!enemy.getMovementPattern().equals("UNKNOWN")) {
                Log.d(TAG, "Enemy " + enemy.getId() + " using " + enemy.getMovementPattern() + 
                      " pattern, threat level: " + enemy.getThreatLevel());
            }
        }
    }
    
    /**
     * Clean up stale enemy tracks
     */
    private void cleanupStaleEnemies() {
        Iterator<Map.Entry<String, EnemyModel>> iterator = trackedEnemies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, EnemyModel> entry = iterator.next();
            EnemyModel enemy = entry.getValue();
            
            if (enemy.getTimeSinceLastSeen() > ENEMY_TIMEOUT_MS) {
                Log.d(TAG, "Removing stale enemy: " + entry.getKey());
                iterator.remove();
            }
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
     * Get the optimal aim point for targeting an enemy
     * @param enemyId The ID of the tracked enemy
     * @return Point to aim at, or null if enemy not found
     */
    public Point getAimPoint(String enemyId) {
        // Check if we have this enemy tracked
        EnemyModel enemy = trackedEnemies.get(enemyId);
        if (enemy == null) {
            return null;
        }
        
        // Get the optimal aiming point (usually head for humanoid targets)
        return enemy.getAimPoint();
    }
    
    /**
     * Get the predicted position of an enemy at a future time
     * @param enemyId The ID of the tracked enemy
     * @param predictTimeMs Time in future to predict for (milliseconds)
     * @return Predicted position, or null if enemy not found
     */
    public Point getPredictedPosition(String enemyId, long predictTimeMs) {
        // Check if we have this enemy tracked
        EnemyModel enemy = trackedEnemies.get(enemyId);
        if (enemy == null) {
            return null;
        }
        
        // Return the predicted position
        return enemy.predictPosition(predictTimeMs);
    }
    
    /**
     * Get the most threatening enemy
     * @return ID of the most threatening enemy, or null if none
     */
    public String getMostThreateningEnemy() {
        if (trackedEnemies.isEmpty()) {
            return null;
        }
        
        String mostThreatening = null;
        float highestThreat = 0;
        
        for (Map.Entry<String, EnemyModel> entry : trackedEnemies.entrySet()) {
            float threat = entry.getValue().getThreatLevel();
            if (threat > highestThreat) {
                highestThreat = threat;
                mostThreatening = entry.getKey();
            }
        }
        
        return mostThreatening;
    }
    
    /**
     * Calculate the timing for a frame-perfect action
     * @param enemyId The target enemy ID
     * @param actionType Type of action (e.g. shot, reload, etc.)
     * @return Optimal delay in milliseconds, or -1 if calculation not possible
     */
    public long calculateFramePerfectTiming(String enemyId, int actionType) {
        // First try using the dedicated TimingOptimizer
        if (timingOptimizer != null) {
            // Get the enemy from tracked enemies
            EnemyModel enemy = trackedEnemies.get(enemyId);
            if (enemy != null) {
                Point enemyPosition = enemy.getCenter();
                String movementPattern = enemy.getMovementPattern();
                float enemySpeed = enemy.getSpeed();
                
                // Try to use the advanced timing optimizer
                long optimizedTiming = timingOptimizer.calculateOptimalTiming(
                    actionType, 
                    enemyPosition, 
                    movementPattern,
                    enemySpeed,
                    timingAssistLevel
                );
                
                if (optimizedTiming >= 0) {
                    Log.d(TAG, "Using TimingOptimizer for action timing: " + optimizedTiming + "ms");
                    return optimizedTiming;
                }
            }
        }
        
        // Fallback to simple built-in timing logic
        Log.d(TAG, "Falling back to built-in timing logic");
        
        // Check if we have this enemy tracked
        EnemyModel enemy = trackedEnemies.get(enemyId);
        if (enemy == null) {
            return -1;
        }
        
        // Base timings on enemy movement pattern
        String pattern = enemy.getMovementPattern();
        
        // Different timing strategies for different movement patterns
        switch (pattern) {
            case "STATIONARY":
                // No delay needed for stationary targets
                return 0;
                
            case "ERRATIC":
                // Hard to predict, use minimal delay
                return 50;
                
            case "STRAFING":
                // For strafing enemies, time based on direction changes
                // This is a simplified approach - real implementation would analyze
                // the actual movement pattern frequency
                return 200;
                
            case "VERTICAL":
                // For vertical movement, timing depends on jump/fall mechanics
                return 150;
                
            default:
                // Default timing
                return 100;
        }
    }
    
    /**
     * Detect hits on enemies
     * @param hitPosition Position of the hit
     * @param hitRadius Radius of the hit
     * @return List of enemy IDs that were hit
     */
    public List<String> detectHits(Point hitPosition, float hitRadius) {
        List<String> hitEnemies = new ArrayList<>();
        
        for (Map.Entry<String, EnemyModel> entry : trackedEnemies.entrySet()) {
            EnemyModel enemy = entry.getValue();
            Point enemyCenter = enemy.getCenter();
            
            // Calculate distance between hit and enemy center
            float distance = calculateDistance(hitPosition, enemyCenter);
            
            // Check if hit was within range
            if (distance <= hitRadius) {
                hitEnemies.add(entry.getKey());
                
                // Record hit for performance tracking
                hitCount++;
                
                Log.d(TAG, "Hit detected on enemy: " + entry.getKey());
            }
        }
        
        // If no hits were recorded, count as a miss
        if (hitEnemies.isEmpty()) {
            missCount++;
        }
        
        // Check if it's time to adjust assistance based on performance
        checkPerformanceAndAdjustAssistance();
        
        return hitEnemies;
    }
    
    /**
     * Record a kill for performance tracking
     */
    public void recordKill() {
        killCount++;
        checkPerformanceAndAdjustAssistance();
    }
    
    /**
     * Record a death for performance tracking
     */
    public void recordDeath() {
        deathCount++;
        checkPerformanceAndAdjustAssistance();
    }
    
    /**
     * Check performance metrics and adjust assistance levels if using auto mode
     */
    private void checkPerformanceAndAdjustAssistance() {
        long currentTime = System.currentTimeMillis();
        
        // Only adjust periodically
        if (currentTime - lastPerformanceAdjustment < PERFORMANCE_ADJUSTMENT_INTERVAL_MS) {
            return;
        }
        
        // Reset timer
        lastPerformanceAdjustment = currentTime;
        
        // Only adjust if in auto mode
        if (aimAssistLevel == ASSIST_LEVEL_AUTO || 
            timingAssistLevel == ASSIST_LEVEL_AUTO || 
            movementAssistLevel == ASSIST_LEVEL_AUTO || 
            tacticalAssistLevel == ASSIST_LEVEL_AUTO) {
            
            // Calculate hit ratio (with protection against division by zero)
            float hitRatio = (hitCount + missCount > 0) ? 
                    (float) hitCount / (hitCount + missCount) : 0.5f;
            
            // Calculate KD ratio (with protection against division by zero)
            float kdRatio = (deathCount > 0) ? 
                    (float) killCount / deathCount : (killCount > 0 ? 2.0f : 1.0f);
            
            // Determine appropriate assistance level based on performance
            int recommendedAssistLevel;
            
            if (hitRatio < 0.3f || kdRatio < 0.5f) {
                // Poor performance - suggest significant assistance
                recommendedAssistLevel = ASSIST_LEVEL_SIGNIFICANT;
            } else if (hitRatio < 0.5f || kdRatio < 1.0f) {
                // Below average - suggest moderate assistance
                recommendedAssistLevel = ASSIST_LEVEL_MODERATE;
            } else if (hitRatio < 0.7f || kdRatio < 1.5f) {
                // Average - suggest subtle assistance
                recommendedAssistLevel = ASSIST_LEVEL_SUBTLE;
            } else {
                // Good performance - minimal assistance needed
                recommendedAssistLevel = ASSIST_LEVEL_SUBTLE;
            }
            
            // Apply recommended level to those in auto mode
            if (aimAssistLevel == ASSIST_LEVEL_AUTO) {
                setAimAssistLevel(recommendedAssistLevel);
            }
            
            if (timingAssistLevel == ASSIST_LEVEL_AUTO) {
                setTimingAssistLevel(recommendedAssistLevel);
            }
            
            if (movementAssistLevel == ASSIST_LEVEL_AUTO) {
                setMovementAssistLevel(recommendedAssistLevel);
            }
            
            if (tacticalAssistLevel == ASSIST_LEVEL_AUTO) {
                setTacticalAssistLevel(recommendedAssistLevel);
            }
            
            // Log the adjustment
            Log.d(TAG, "Adjusted assist levels based on performance: " +
                  "Hit Ratio=" + hitRatio + ", KD Ratio=" + kdRatio + 
                  ", Recommended Level=" + recommendedAssistLevel);
            
            // Reset counters for next period
            hitCount = 0;
            missCount = 0;
            // Don't reset kill/death as they're less frequent and need more data
        }
    }
    
    /**
     * Set the aim assistance level
     * @param level Assistance level (NONE, SUBTLE, MODERATE, SIGNIFICANT, AUTO)
     */
    public void setAimAssistLevel(int level) {
        if (level >= ASSIST_LEVEL_NONE && level <= ASSIST_LEVEL_AUTO) {
            this.aimAssistLevel = level;
            Log.d(TAG, "Aim assist level set to " + getAssistLevelName(level));
        }
    }
    
    /**
     * Set the timing assistance level
     * @param level Assistance level (NONE, SUBTLE, MODERATE, SIGNIFICANT, AUTO)
     */
    public void setTimingAssistLevel(int level) {
        if (level >= ASSIST_LEVEL_NONE && level <= ASSIST_LEVEL_AUTO) {
            this.timingAssistLevel = level;
            Log.d(TAG, "Timing assist level set to " + getAssistLevelName(level));
        }
    }
    
    /**
     * Set the movement assistance level
     * @param level Assistance level (NONE, SUBTLE, MODERATE, SIGNIFICANT, AUTO)
     */
    public void setMovementAssistLevel(int level) {
        if (level >= ASSIST_LEVEL_NONE && level <= ASSIST_LEVEL_AUTO) {
            this.movementAssistLevel = level;
            Log.d(TAG, "Movement assist level set to " + getAssistLevelName(level));
        }
    }
    
    /**
     * Set the tactical assistance level
     * @param level Assistance level (NONE, SUBTLE, MODERATE, SIGNIFICANT, AUTO)
     */
    public void setTacticalAssistLevel(int level) {
        if (level >= ASSIST_LEVEL_NONE && level <= ASSIST_LEVEL_AUTO) {
            this.tacticalAssistLevel = level;
            Log.d(TAG, "Tactical assist level set to " + getAssistLevelName(level));
        }
    }
    
    /**
     * Get a string representation of an assist level
     */
    private String getAssistLevelName(int level) {
        switch(level) {
            case ASSIST_LEVEL_NONE:
                return "None";
            case ASSIST_LEVEL_SUBTLE:
                return "Subtle";
            case ASSIST_LEVEL_MODERATE:
                return "Moderate";
            case ASSIST_LEVEL_SIGNIFICANT:
                return "Significant";
            case ASSIST_LEVEL_AUTO:
                return "Auto";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get current aim assist magnitude based on the current level setting
     * @return Assist magnitude (0.0 to 1.0)
     */
    public float getAimAssistMagnitude() {
        switch(aimAssistLevel) {
            case ASSIST_LEVEL_NONE:
                return 0.0f;
            case ASSIST_LEVEL_SUBTLE:
                return 0.25f;
            case ASSIST_LEVEL_MODERATE:
                return 0.5f;
            case ASSIST_LEVEL_SIGNIFICANT:
                return 0.75f;
            case ASSIST_LEVEL_AUTO:
                // Auto should have been adjusted to one of the other levels
                return 0.5f;
            default:
                return 0.0f;
        }
    }
    
    /**
     * Get current timing assist magnitude based on the current level setting
     * @return Assist magnitude (0.0 to 1.0)
     */
    public float getTimingAssistMagnitude() {
        switch(timingAssistLevel) {
            case ASSIST_LEVEL_NONE:
                return 0.0f;
            case ASSIST_LEVEL_SUBTLE:
                return 0.25f;
            case ASSIST_LEVEL_MODERATE:
                return 0.5f;
            case ASSIST_LEVEL_SIGNIFICANT:
                return 0.75f;
            case ASSIST_LEVEL_AUTO:
                // Auto should have been adjusted to one of the other levels
                return 0.5f;
            default:
                return 0.0f;
        }
    }
    
    /**
     * Apply aim assistance to a targeting point
     * @param originalAimPoint Original aim point
     * @param targetEnemyId Enemy to assist targeting for, or null to find closest
     * @return Adjusted aim point
     */
    public Point applyAimAssistance(Point originalAimPoint, String targetEnemyId) {
        // If aim assistance is disabled, return original point
        if (aimAssistLevel == ASSIST_LEVEL_NONE || !enableAimAssistance) {
            return originalAimPoint;
        }
        
        // Try using the dedicated AimAssistant first
        if (aimAssistant != null) {
            // Gather information about the target
            EnemyModel targetEnemy = null;
            List<Point> allEnemyPositions = new ArrayList<>();
            
            // Collect relevant enemy information
            if (targetEnemyId != null && trackedEnemies.containsKey(targetEnemyId)) {
                targetEnemy = trackedEnemies.get(targetEnemyId);
            }
            
            // Collect positions of all enemies
            for (EnemyModel enemy : trackedEnemies.values()) {
                allEnemyPositions.add(enemy.getCenter());
            }
            
            // If we have a dedicated enemy target, use it for advanced aim assistance
            if (targetEnemy != null) {
                Point resultPoint = aimAssistant.calculateAimPoint(
                    originalAimPoint,
                    targetEnemy.getCenter(),
                    targetEnemy.getAimPoint(),
                    targetEnemy.getPredictedPosition(200),  // 200ms prediction
                    targetEnemy.getMovementPattern(),
                    aimAssistLevel,
                    allEnemyPositions
                );
                
                if (resultPoint != null) {
                    Log.d(TAG, "Using AimAssistant for aim assistance");
                    return resultPoint;
                }
            } 
            // Try to find best enemy to target if none specified
            else if (!allEnemyPositions.isEmpty()) {
                Point resultPoint = aimAssistant.findAndAssistAim(
                    originalAimPoint,
                    allEnemyPositions,
                    aimAssistLevel
                );
                
                if (resultPoint != null) {
                    Log.d(TAG, "Using AimAssistant for target acquisition and aim");
                    return resultPoint;
                }
            }
        }
        
        // Fall back to built-in aim assistance if AimAssistant fails
        Log.d(TAG, "Falling back to built-in aim assistance logic");
        
        // Find target enemy
        EnemyModel targetEnemy = null;
        
        if (targetEnemyId != null) {
            // Use specified enemy
            targetEnemy = trackedEnemies.get(targetEnemyId);
        } else if (!trackedEnemies.isEmpty()) {
            // Find closest enemy to aim point
            EnemyModel closest = null;
            float minDistance = Float.MAX_VALUE;
            
            for (EnemyModel enemy : trackedEnemies.values()) {
                float distance = calculateDistance(originalAimPoint, enemy.getCenter());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = enemy;
                }
            }
            
            targetEnemy = closest;
        }
        
        // If no target found, return original point
        if (targetEnemy == null) {
            return originalAimPoint;
        }
        
        // Get optimal aim point for this enemy
        Point optimalAimPoint = targetEnemy.getAimPoint();
        
        // Calculate adjustment based on assistance level
        float assistMagnitude = getAimAssistMagnitude();
        
        // Interpolate between original and optimal point based on assistance magnitude
        return new Point(
                (int)(originalAimPoint.x + (optimalAimPoint.x - originalAimPoint.x) * assistMagnitude),
                (int)(originalAimPoint.y + (optimalAimPoint.y - originalAimPoint.y) * assistMagnitude)
        );
    }
    
    /**
     * Detect enemies in the screenshot
     * @param screenshot The current screen bitmap
     * @return List of rectangles containing potential enemies
     */
    public List<Rect> detectEnemies(Bitmap screenshot) {
        List<Rect> enemyRects = new ArrayList<>();
        
        try {
            // First try using the advanced EnemyDetector class
            if (enemyDetector != null) {
                enemyRects = enemyDetector.detectEnemies(screenshot, currentGamePackage);
                
                // Log detection source for debugging
                if (enemyRects != null && !enemyRects.isEmpty()) {
                    Log.d(TAG, "Enemies detected using specialized EnemyDetector: " + enemyRects.size());
                }
            }
            
            // If no enemies found or detector failed, fall back to built-in methods
            if ((enemyRects == null || enemyRects.isEmpty()) && enhancedEnemyDetection && openCvInitialized) {
                // Use advanced OpenCV-based detection
                enemyRects = detectEnemiesAdvanced(screenshot);
                Log.d(TAG, "Fallback to advanced OpenCV detection: " + 
                     (enemyRects != null ? enemyRects.size() : 0) + " enemies found");
            } 
            
            // Final fallback to basic detection if all else fails
            if (enemyRects == null || enemyRects.isEmpty()) {
                // Fallback to basic detection
                enemyRects = detectEnemiesBasic(screenshot);
                Log.d(TAG, "Fallback to basic detection: " + 
                     (enemyRects != null ? enemyRects.size() : 0) + " enemies found");
            }
            
            // Apply additional filters if needed
            if (enemyRects != null && !enemyRects.isEmpty()) {
                // Filter out enemies that are too small
                Iterator<Rect> iterator = enemyRects.iterator();
                while (iterator.hasNext()) {
                    Rect rect = iterator.next();
                    if (rect.width() < minEnemySize || rect.height() < minEnemySize) {
                        iterator.remove();
                    }
                }
            }
            
            // Update combat pattern recognition
            if (enemyRects != null && !enemyRects.isEmpty() && combatPatternRecognizer != null) {
                combatPatternRecognizer.analyzeEnemyPositions(enemyRects, screenshot);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enemy detection", e);
            enemyRects = new ArrayList<>();
        }
        
        return enemyRects;
    }
    
    /**
     * Basic enemy detection using simple color and movement analysis
     * @param screenshot The current screen bitmap
     * @return List of rectangles containing potential enemies
     */
    private List<Rect> detectEnemiesBasic(Bitmap screenshot) {
        List<Rect> enemyRects = new ArrayList<>();
        
        // Get the main game area (excluding UI elements)
        Rect gameArea = getGameArea(screenshot);
        
        // Sample points in the game area
        int sampleStepX = Math.max(1, gameArea.width() / 20);
        int sampleStepY = Math.max(1, gameArea.height() / 20);
        
        // Find unusual colors that might represent enemies
        for (int y = gameArea.top; y < gameArea.bottom; y += sampleStepY) {
            for (int x = gameArea.left; x < gameArea.right; x += sampleStepX) {
                int pixel = screenshot.getPixel(x, y);
                
                // Check for colors that might represent enemies
                // This is a simplified approach and would need game-specific tuning
                if (isLikelyEnemyColor(pixel)) {
                    // Found potential enemy, look for its boundaries
                    Rect potentialEnemy = expandRegion(screenshot, x, y, gameArea);
                    
                    // Check minimum size and other criteria
                    if (potentialEnemy.width() > minEnemySize && 
                        potentialEnemy.height() > minEnemySize) {
                        enemyRects.add(potentialEnemy);
                    }
                }
            }
        }
        
        return enemyRects;
    }
    
    /**
     * Advanced enemy detection using OpenCV
     * @param screenshot The current screen bitmap
     * @return List of rectangles containing potential enemies
     */
    private List<Rect> detectEnemiesAdvanced(Bitmap screenshot) {
        List<Rect> enemyRects = new ArrayList<>();
        
        // Convert Bitmap to OpenCV Mat
        Mat rgbaMat = new Mat();
        Utils.bitmapToMat(screenshot, rgbaMat);
        
        // Convert to HSV color space for better color segmentation
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(rgbaMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        
        // Apply filters to highlight potential enemies
        List<MatOfPoint> contours = new ArrayList<>();
        
        try {
            // Use color detection
            if (useColorDetection) {
                Mat colorMask = new Mat();
                
                // Create a mask for common enemy colors (would need game-specific tuning)
                // For example, in many FPS games, enemies have distinct red/orange highlights
                Core.inRange(hsvMat, 
                        new Scalar(0, 100, 100),   // Lower bound for reddish colors
                        new Scalar(10, 255, 255),  // Upper bound for reddish colors
                        colorMask);
                
                // Also detect higher-red spectrum (wraps around in HSV)
                Mat colorMask2 = new Mat();
                Core.inRange(hsvMat, 
                        new Scalar(170, 100, 100), // Lower bound for reddish colors
                        new Scalar(180, 255, 255), // Upper bound for reddish colors
                        colorMask2);
                
                // Combine masks
                Core.bitwise_or(colorMask, colorMask2, colorMask);
                
                // Find contours in the mask
                Mat hierarchy = new Mat();
                Imgproc.findContours(colorMask, contours, hierarchy, 
                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Clean up
                colorMask.release();
                colorMask2.release();
                hierarchy.release();
            }
            
            // Use contour analysis for shape detection
            if (useContourAnalysis) {
                // Convert to grayscale
                Mat grayMat = new Mat();
                Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY);
                
                // Apply Gaussian blur to reduce noise
                Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);
                
                // Use Canny edge detector
                Mat edges = new Mat();
                Imgproc.Canny(grayMat, edges, 50, 150);
                
                // Dilate to connect nearby edges
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.dilate(edges, edges, kernel);
                
                // Find contours
                List<MatOfPoint> edgeContours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(edges, edgeContours, hierarchy, 
                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Filter contours by size and shape
                for (MatOfPoint contour : edgeContours) {
                    if (Imgproc.contourArea(contour) > minEnemySize * minEnemySize) {
                        // Analyze shape - convert to MatOfPoint2f for more accurate analysis
                        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                        
                        // Check aspect ratio - enemy characters usually have specific proportions
                        org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                        float aspectRatio = rect.width / (float) rect.height;
                        
                        // Most humanoid characters have aspect ratios between 0.3 and 0.8
                        if (aspectRatio > 0.3f && aspectRatio < 0.8f) {
                            contours.add(contour);
                        } else {
                            contour.release();
                        }
                        
                        contour2f.release();
                    } else {
                        contour.release();
                    }
                }
                
                // Clean up
                grayMat.release();
                edges.release();
                kernel.release();
                hierarchy.release();
            }
            
            // Use movement detection
            if (useMovementDetection && previousFrame != null) {
                // Convert previous frame to Mat
                Mat prevMat = new Mat();
                Utils.bitmapToMat(previousFrame, prevMat);
                
                // Calculate absolute difference between frames
                Mat diffMat = new Mat();
                Core.absdiff(rgbaMat, prevMat, diffMat);
                
                // Convert to grayscale
                Mat grayDiff = new Mat();
                Imgproc.cvtColor(diffMat, grayDiff, Imgproc.COLOR_RGB2GRAY);
                
                // Apply threshold to identify significant changes
                Mat threshMat = new Mat();
                Imgproc.threshold(grayDiff, threshMat, movementThreshold, 255, Imgproc.THRESH_BINARY);
                
                // Apply morphological operations to clean up the mask
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
                Imgproc.morphologyEx(threshMat, threshMat, Imgproc.MORPH_CLOSE, kernel);
                
                // Find contours in the movement mask
                List<MatOfPoint> movementContours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(threshMat, movementContours, hierarchy, 
                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Filter by size
                for (MatOfPoint contour : movementContours) {
                    if (Imgproc.contourArea(contour) > minEnemySize * minEnemySize) {
                        contours.add(contour);
                    } else {
                        contour.release();
                    }
                }
                
                // Clean up
                prevMat.release();
                diffMat.release();
                grayDiff.release();
                threshMat.release();
                kernel.release();
                hierarchy.release();
            }
            
            // Process all detected contours
            for (MatOfPoint contour : contours) {
                org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                
                // Convert OpenCV Rect to Android Rect
                Rect androidRect = new Rect(
                        rect.x, 
                        rect.y, 
                        rect.x + rect.width, 
                        rect.y + rect.height
                );
                
                // Add to results if it passes all criteria
                if (rect.width > minEnemySize && rect.height > minEnemySize &&
                    validateEnemyCandidate(screenshot, androidRect)) {
                    enemyRects.add(androidRect);
                }
                
                contour.release();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in OpenCV processing", e);
        } finally {
            // Clean up
            rgbaMat.release();
            hsvMat.release();
        }
        
        // Merge overlapping detections
        return mergeOverlappingRects(enemyRects);
    }
    
    /**
     * Merge overlapping rectangles to avoid duplicate detections
     * @param rects List of rectangles
     * @return Merged list
     */
    private List<Rect> mergeOverlappingRects(List<Rect> rects) {
        List<Rect> merged = new ArrayList<>();
        
        for (Rect rect : rects) {
            boolean shouldAdd = true;
            
            for (int i = 0; i < merged.size(); i++) {
                Rect existing = merged.get(i);
                
                if (Rect.intersects(rect, existing)) {
                    // Merge by creating a new rectangle that encompasses both
                    merged.set(i, new Rect(
                            Math.min(rect.left, existing.left),
                            Math.min(rect.top, existing.top),
                            Math.max(rect.right, existing.right),
                            Math.max(rect.bottom, existing.bottom)
                    ));
                    
                    shouldAdd = false;
                    break;
                }
            }
            
            if (shouldAdd) {
                merged.add(rect);
            }
        }
        
        return merged;
    }
    
    /**
     * Check if a color is likely to represent an enemy
     * @param pixel The color value
     * @return True if the color might represent an enemy
     */
    private boolean isLikelyEnemyColor(int pixel) {
        // Extract RGB components
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        
        // Many games highlight enemies with red/orange colors
        boolean isReddish = r > 150 && r > g * 1.5 && r > b * 1.5;
        
        // Some games use bright outlines
        boolean isBright = (r + g + b) > 600;
        
        // Some use high contrast
        boolean isHighContrast = Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b) > 150;
        
        return isReddish || (isBright && isHighContrast);
    }
    
    /**
     * Expand a region around a point to find the boundaries of a potential enemy
     * @param bitmap The source bitmap
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param bounds Boundary limits
     * @return Rectangle encompassing the potential enemy
     */
    private Rect expandRegion(Bitmap bitmap, int startX, int startY, Rect bounds) {
        int left = startX;
        int right = startX;
        int top = startY;
        int bottom = startY;
        
        int startColor = bitmap.getPixel(startX, startY);
        
        // Expand left
        for (int x = startX - 1; x >= bounds.left; x--) {
            if (isColorSimilar(bitmap.getPixel(x, startY), startColor)) {
                left = x;
            } else {
                break;
            }
        }
        
        // Expand right
        for (int x = startX + 1; x < bounds.right; x++) {
            if (isColorSimilar(bitmap.getPixel(x, startY), startColor)) {
                right = x;
            } else {
                break;
            }
        }
        
        // Expand up
        for (int y = startY - 1; y >= bounds.top; y--) {
            if (isColorSimilar(bitmap.getPixel(startX, y), startColor)) {
                top = y;
            } else {
                break;
            }
        }
        
        // Expand down
        for (int y = startY + 1; y < bounds.bottom; y++) {
            if (isColorSimilar(bitmap.getPixel(startX, y), startColor)) {
                bottom = y;
            } else {
                break;
            }
        }
        
        // Add some padding
        int padding = 5;
        left = Math.max(bounds.left, left - padding);
        top = Math.max(bounds.top, top - padding);
        right = Math.min(bounds.right, right + padding);
        bottom = Math.min(bounds.bottom, bottom + padding);
        
        return new Rect(left, top, right, bottom);
    }
    
    /**
     * Check if two colors are similar
     * @param color1 First color
     * @param color2 Second color
     * @return True if colors are similar
     */
    private boolean isColorSimilar(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        int threshold = 30;
        return Math.abs(r1 - r2) < threshold &&
               Math.abs(g1 - g2) < threshold &&
               Math.abs(b1 - b2) < threshold;
    }
    
    /**
     * Get the main game area (excluding UI elements)
     * @param screenshot The current screen bitmap
     * @return Rectangle representing the main game area
     */
    private Rect getGameArea(Bitmap screenshot) {
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Most FPS games have UI elements at the edges, especially top and bottom
        // This is a simplified approach; a real implementation might use UI detection
        int marginX = (int)(width * 0.05f);  // 5% margin on sides
        int marginTop = (int)(height * 0.1f); // 10% margin on top
        int marginBottom = (int)(height * 0.15f); // 15% margin on bottom
        
        return new Rect(
                marginX,
                marginTop,
                width - marginX,
                height - marginBottom
        );
    }
    
    /**
     * Validate an enemy candidate to reduce false positives
     * @param bitmap The source bitmap
     * @param rect The candidate rectangle
     * @return True if the candidate passes validation
     */
    private boolean validateEnemyCandidate(Bitmap bitmap, Rect rect) {
        // Check aspect ratio - most character silhouettes have specific proportions
        float aspectRatio = rect.width() / (float) rect.height();
        if (aspectRatio < 0.3f || aspectRatio > 0.8f) {
            return false;
        }
        
        // Check for high color variance within the region - enemies often have
        // distinctive color patterns compared to the background
        double variance = calculateColorVariance(bitmap, rect);
        if (variance < 200) { // Threshold can be tuned per game
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate color variance within a region
     * @param bitmap The source bitmap
     * @param rect The region to analyze
     * @return Variance value
     */
    private double calculateColorVariance(Bitmap bitmap, Rect rect) {
        // Sample pixels in the region
        int sampleStep = Math.max(1, Math.min(rect.width(), rect.height()) / 10);
        
        List<Integer> rValues = new ArrayList<>();
        List<Integer> gValues = new ArrayList<>();
        List<Integer> bValues = new ArrayList<>();
        
        for (int y = rect.top; y < rect.bottom; y += sampleStep) {
            for (int x = rect.left; x < rect.right; x += sampleStep) {
                if (x >= 0 && y >= 0 && x < bitmap.getWidth() && y < bitmap.getHeight()) {
                    int pixel = bitmap.getPixel(x, y);
                    rValues.add(Color.red(pixel));
                    gValues.add(Color.green(pixel));
                    bValues.add(Color.blue(pixel));
                }
            }
        }
        
        // Calculate variance
        double rVariance = calculateVariance(rValues);
        double gVariance = calculateVariance(gValues);
        double bVariance = calculateVariance(bValues);
        
        return rVariance + gVariance + bVariance;
    }
    
    /**
     * Calculate variance of a list of values
     * @param values List of integer values
     * @return Variance
     */
    private double calculateVariance(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        
        // Calculate mean
        double sum = 0;
        for (int value : values) {
            sum += value;
        }
        double mean = sum / values.size();
        
        // Calculate variance
        double variance = 0;
        for (int value : values) {
            variance += Math.pow(value - mean, 2);
        }
        
        return variance / values.size();
    }
    
    /**
     * Add an enemy to the game state
     * @param center Center point of the enemy
     * @param bounds Bounding rectangle of the enemy
     */
    private void addEnemyToGameState(Point center, Rect bounds) {
        // Calculate distance from center of screen (could be used for aim priority)
        int screenCenterX = getScreenWidth() / 2;
        int screenCenterY = getScreenHeight() / 2;
        
        double distance = Math.sqrt(
                Math.pow(center.x - screenCenterX, 2) +
                Math.pow(center.y - screenCenterY, 2)
        );
        
        // Calculate size as percentage of screen
        float size = (bounds.width() * bounds.height()) / 
                     (float)(getScreenWidth() * getScreenHeight());
        
        // Store in our custom game state format
        EnemyInfo enemy = new EnemyInfo(
                center.x,
                center.y,
                bounds.width(),
                bounds.height(),
                distance,
                size
        );
        
        currentGameState.put("enemy_" + System.currentTimeMillis(), enemy);
        
        // Store total enemy count
        int currentCount = 0;
        if (currentGameState.containsKey("enemy_count")) {
            currentCount = (int) currentGameState.get("enemy_count");
        }
        currentGameState.put("enemy_count", currentCount + 1);
    }
    
    /**
     * Provide aim assistance based on detected enemies
     * @return Target coordinates, or null if no valid target
     */
    public Point getAimAssistTarget() {
        if (!enableAimAssistance) {
            return null;
        }
        
        EnemyInfo bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        
        // Find the best target based on distance from crosshair
        for (Object value : currentGameState.values()) {
            if (value instanceof EnemyInfo) {
                EnemyInfo enemy = (EnemyInfo) value;
                
                // Calculate a score based on distance and size
                // Lower score is better (closer to center, larger size)
                double score = enemy.distanceFromCenter * (1.0 - enemy.relativeSize * 2.0);
                
                if (score < bestScore) {
                    bestScore = score;
                    bestTarget = enemy;
                }
            }
        }
        
        if (bestTarget != null) {
            // Aim for upper body (slight offset from center)
            return new Point(
                    bestTarget.centerX,
                    bestTarget.centerY - (int)(bestTarget.height * 0.2f)
            );
        }
        
        return null;
    }
    
    /**
     * Draw debugging visualization on the canvas
     * @param canvas Canvas to draw on
     */
    public void drawDebugVisualization(Canvas canvas) {
        // Draw detected enemies
        Paint enemyPaint = new Paint();
        enemyPaint.setColor(Color.RED);
        enemyPaint.setStyle(Paint.Style.STROKE);
        enemyPaint.setStrokeWidth(3);
        
        Paint targetPaint = new Paint();
        targetPaint.setColor(Color.GREEN);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(5);
        
        // Draw all detected enemies
        for (Object value : currentGameState.values()) {
            if (value instanceof EnemyInfo) {
                EnemyInfo enemy = (EnemyInfo) value;
                
                Rect rect = new Rect(
                        enemy.centerX - enemy.width / 2,
                        enemy.centerY - enemy.height / 2,
                        enemy.centerX + enemy.width / 2,
                        enemy.centerY + enemy.height / 2
                );
                
                canvas.drawRect(rect, enemyPaint);
            }
        }
        
        // Draw aim target
        Point aimTarget = getAimAssistTarget();
        if (aimTarget != null) {
            canvas.drawCircle(aimTarget.x, aimTarget.y, 20, targetPaint);
            canvas.drawLine(aimTarget.x - 30, aimTarget.y, aimTarget.x + 30, aimTarget.y, targetPaint);
            canvas.drawLine(aimTarget.x, aimTarget.y - 30, aimTarget.x, aimTarget.y + 30, targetPaint);
        }
    }
    
    /**
     * Calculate the color difference between two pixels
     * Used for contrast detection
     * @param color1 First color
     * @param color2 Second color
     * @return Difference value
     */
    private float colorDifference(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        // Calculate Euclidean distance in RGB space
        return (float) Math.sqrt(
            Math.pow(r1 - r2, 2) +
            Math.pow(g1 - g2, 2) +
            Math.pow(b1 - b2, 2)
        );
    }
    
    /**
     * Inner class to store enemy information
     */
    private static class EnemyInfo {
        int centerX;
        int centerY;
        int width;
        int height;
        double distanceFromCenter;
        float relativeSize;
        
        public EnemyInfo(int centerX, int centerY, int width, int height, 
                        double distanceFromCenter, float relativeSize) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
            this.distanceFromCenter = distanceFromCenter;
            this.relativeSize = relativeSize;
        }
    }
    
    // Settings and configuration
    public void setEnhancedEnemyDetection(boolean enabled) {
        this.enhancedEnemyDetection = enabled;
    }
    
    public void setUseContourAnalysis(boolean enabled) {
        this.useContourAnalysis = enabled;
    }
    
    public void setUseColorDetection(boolean enabled) {
        this.useColorDetection = enabled;
    }
    
    public void setUseMovementDetection(boolean enabled) {
        this.useMovementDetection = enabled;
    }
    
    public void setEnableAimAssistance(boolean enabled) {
        this.enableAimAssistance = enabled;
    }
    
    public void setEnemyConfidenceThreshold(float threshold) {
        this.enemyConfidenceThreshold = threshold;
    }
    
    public void setMovementThreshold(float threshold) {
        this.movementThreshold = threshold;
    }
    
    public void setMinEnemySize(int size) {
        this.minEnemySize = size;
    }
}
