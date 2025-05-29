package com.aiassistant.core.gaming.fps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced system for recognizing combat patterns in FPS games.
 * Analyzes enemy movement, positioning and behavior to predict actions and optimize player response.
 */
public class CombatPatternRecognizer {
    private static final String TAG = "CombatPatternRecognizer";
    
    // Pattern recognition thresholds
    private static final int MIN_SAMPLES_FOR_PATTERN = 5;
    private static final int MAX_SAMPLES_TO_STORE = 50;
    private static final long MAX_PATTERN_AGE_MS = 10000; // 10 seconds
    
    // Movement pattern types
    public static final String PATTERN_UNKNOWN = "UNKNOWN";
    public static final String PATTERN_STATIONARY = "STATIONARY";
    public static final String PATTERN_STRAFING = "STRAFING";
    public static final String PATTERN_ERRATIC = "ERRATIC";
    public static final String PATTERN_LINEAR = "LINEAR";
    public static final String PATTERN_VERTICAL = "VERTICAL";
    public static final String PATTERN_CIRCLING = "CIRCLING";
    public static final String PATTERN_PEEKING = "PEEKING";
    
    // Tactical pattern types
    public static final String TACTIC_UNKNOWN = "UNKNOWN";
    public static final String TACTIC_AGGRESSIVE = "AGGRESSIVE";
    public static final String TACTIC_DEFENSIVE = "DEFENSIVE";
    public static final String TACTIC_FLANKING = "FLANKING";
    public static final String TACTIC_AMBUSH = "AMBUSH";
    public static final String TACTIC_BAIT = "BAIT";
    public static final String TACTIC_RETREAT = "RETREAT";
    public static final String TACTIC_FORMATION = "FORMATION";
    public static final String TACTIC_SUPPRESSIVE = "SUPPRESSIVE";
    
    // Context
    private final Context context;
    
    // Historical data for pattern recognition
    private final Map<String, List<EnemyPositionSample>> enemyPositionHistory = new HashMap<>();
    private final Map<String, String> recognizedMovementPatterns = new HashMap<>();
    private final Map<String, String> recognizedTacticalPatterns = new HashMap<>();
    private final Map<String, Float> enemyThreatLevels = new HashMap<>();
    
    // Statistics for performance analysis
    private int totalPatternsAnalyzed = 0;
    private int successfulPatternRecognitions = 0;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CombatPatternRecognizer(Context context) {
        this.context = context;
    }
    
    /**
     * Analyze enemy positions to identify movement and tactical patterns
     * @param enemyRects List of rectangles containing detected enemies
     * @param screenshot Current frame for visual analysis
     */
    public void analyzeEnemyPositions(List<Rect> enemyRects, Bitmap screenshot) {
        if (enemyRects == null || enemyRects.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Generate temporary IDs for enemies that haven't been tracked before
        List<String> currentEnemyIds = new ArrayList<>();
        
        // Process each enemy
        for (int i = 0; i < enemyRects.size(); i++) {
            Rect rect = enemyRects.get(i);
            
            // Create a position sample
            Point center = new Point(
                    rect.left + rect.width() / 2,
                    rect.top + rect.height() / 2
            );
            
            EnemyPositionSample sample = new EnemyPositionSample(
                    center.x, center.y, rect.width(), rect.height(), currentTime);
            
            // Try to match with existing enemies
            String enemyId = findMatchingEnemyId(center, rect, enemyPositionHistory.keySet());
            
            if (enemyId == null) {
                // Create a new ID for this enemy
                enemyId = "enemy_" + i + "_" + currentTime;
            }
            
            currentEnemyIds.add(enemyId);
            
            // Update history
            List<EnemyPositionSample> history = enemyPositionHistory.get(enemyId);
            if (history == null) {
                history = new ArrayList<>();
                enemyPositionHistory.put(enemyId, history);
            }
            
            // Add new sample
            history.add(sample);
            
            // Limit history size to prevent memory issues
            while (history.size() > MAX_SAMPLES_TO_STORE) {
                history.remove(0);
            }
            
            // Analyze patterns
            if (history.size() >= MIN_SAMPLES_FOR_PATTERN) {
                analyzeMovementPattern(enemyId, history);
                analyzeTacticalPattern(enemyId, history, screenshot);
                calculateThreatLevel(enemyId, history);
            }
        }
        
        // Clean up old data
        cleanupStaleData(currentTime, currentEnemyIds);
        
        // Log pattern information for debugging
        logPatternInfo();
    }
    
    /**
     * Find a matching enemy ID from previous frames
     */
    private String findMatchingEnemyId(Point center, Rect rect, Iterable<String> existingIds) {
        String bestMatch = null;
        float bestDistance = Float.MAX_VALUE;
        
        for (String id : existingIds) {
            List<EnemyPositionSample> history = enemyPositionHistory.get(id);
            if (history == null || history.isEmpty()) {
                continue;
            }
            
            // Get the most recent position
            EnemyPositionSample lastSample = history.get(history.size() - 1);
            Point lastCenter = new Point(lastSample.x, lastSample.y);
            
            // Calculate distance
            float distance = calculateDistance(center, lastCenter);
            
            // Check if this is a potential match (based on proximity and size similarity)
            float sizeDiff = Math.abs(rect.width() * rect.height() - lastSample.width * lastSample.height) / 
                            (float)(rect.width() * rect.height());
            
            if (distance < bestDistance && sizeDiff < 0.5f) {
                bestDistance = distance;
                bestMatch = id;
            }
        }
        
        // Only return a match if it's within a reasonable distance
        return (bestDistance < 200) ? bestMatch : null;
    }
    
    /**
     * Analyze movement pattern for an enemy
     */
    private void analyzeMovementPattern(String enemyId, List<EnemyPositionSample> history) {
        totalPatternsAnalyzed++;
        
        if (history.size() < MIN_SAMPLES_FOR_PATTERN) {
            recognizedMovementPatterns.put(enemyId, PATTERN_UNKNOWN);
            return;
        }
        
        // Calculate movement statistics
        float totalDistance = 0;
        float avgXVelocity = 0;
        float avgYVelocity = 0;
        float xVariance = 0;
        float yVariance = 0;
        float directionalVariance = 0;
        
        List<Float> xValues = new ArrayList<>();
        List<Float> yValues = new ArrayList<>();
        List<Float> directionAngles = new ArrayList<>();
        
        // Calculate velocities and distances
        for (int i = 1; i < history.size(); i++) {
            EnemyPositionSample curr = history.get(i);
            EnemyPositionSample prev = history.get(i - 1);
            
            // Time delta in seconds
            float dt = (curr.timestamp - prev.timestamp) / 1000.0f;
            if (dt <= 0) dt = 0.016f; // Default to 60fps (1/60 sec) if timestamps are equal
            
            // Calculate velocities
            float vx = (curr.x - prev.x) / dt;
            float vy = (curr.y - prev.y) / dt;
            
            // Store values for variance calculation
            xValues.add(vx);
            yValues.add(vy);
            
            // Calculate direction angle
            double angle = Math.atan2(vy, vx);
            directionAngles.add((float) angle);
            
            // Accumulate values
            avgXVelocity += vx;
            avgYVelocity += vy;
            totalDistance += calculateDistance(
                    new Point(prev.x, prev.y), 
                    new Point(curr.x, curr.y)
            );
        }
        
        // Calculate averages
        avgXVelocity /= (history.size() - 1);
        avgYVelocity /= (history.size() - 1);
        float avgSpeed = totalDistance / (history.size() - 1);
        
        // Calculate variances
        for (Float vx : xValues) {
            xVariance += Math.pow(vx - avgXVelocity, 2);
        }
        for (Float vy : yValues) {
            yVariance += Math.pow(vy - avgYVelocity, 2);
        }
        
        xVariance /= xValues.size();
        yVariance /= yValues.size();
        
        // Calculate directional variance
        float prevAngle = directionAngles.get(0);
        for (int i = 1; i < directionAngles.size(); i++) {
            float angleDiff = Math.abs(directionAngles.get(i) - prevAngle);
            if (angleDiff > Math.PI) {
                angleDiff = (float) (2 * Math.PI - angleDiff);
            }
            directionalVariance += angleDiff;
            prevAngle = directionAngles.get(i);
        }
        directionalVariance /= (directionAngles.size() - 1);
        
        // Determine pattern based on statistics
        String pattern = PATTERN_UNKNOWN;
        
        // Stationary check
        if (avgSpeed < 10) {
            pattern = PATTERN_STATIONARY;
        }
        // Strafing check (high X variance, low Y variance)
        else if (xVariance > 5000 && yVariance < 2000 && Math.abs(avgYVelocity) < 50) {
            pattern = PATTERN_STRAFING;
        }
        // Vertical movement check (low X variance, high Y variance)
        else if (yVariance > 5000 && xVariance < 2000 && Math.abs(avgXVelocity) < 50) {
            pattern = PATTERN_VERTICAL;
        }
        // Linear movement check (low directional variance)
        else if (directionalVariance < 0.5 && avgSpeed > 50) {
            pattern = PATTERN_LINEAR;
        }
        // Circling check (consistent directional change)
        else if (directionalVariance > 0.5 && directionalVariance < 1.0 && avgSpeed > 20) {
            pattern = PATTERN_CIRCLING;
        }
        // Peeking check (alternating movement with stationary periods)
        else if (hasPeekingPattern(history)) {
            pattern = PATTERN_PEEKING;
        }
        // Erratic movement (high variance in all dimensions)
        else if (xVariance > 3000 && yVariance > 3000 && directionalVariance > 1.0) {
            pattern = PATTERN_ERRATIC;
        }
        
        // Update the recognized pattern
        String previousPattern = recognizedMovementPatterns.get(enemyId);
        if (previousPattern == null || !previousPattern.equals(pattern)) {
            Log.d(TAG, "New movement pattern for enemy " + enemyId + ": " + pattern);
        }
        
        recognizedMovementPatterns.put(enemyId, pattern);
        successfulPatternRecognitions++;
    }
    
    /**
     * Check if the enemy has a peeking pattern (move, stop, move, stop)
     */
    private boolean hasPeekingPattern(List<EnemyPositionSample> history) {
        int movingPeriods = 0;
        int stationaryPeriods = 0;
        boolean wasMoving = false;
        
        for (int i = 1; i < history.size(); i++) {
            EnemyPositionSample curr = history.get(i);
            EnemyPositionSample prev = history.get(i - 1);
            
            float distance = calculateDistance(
                    new Point(prev.x, prev.y),
                    new Point(curr.x, curr.y)
            );
            
            boolean isMoving = distance > 5;
            
            if (isMoving != wasMoving) {
                if (isMoving) {
                    movingPeriods++;
                } else {
                    stationaryPeriods++;
                }
                wasMoving = isMoving;
            }
        }
        
        // Peeking pattern has alternating moving and stationary periods
        return movingPeriods >= 2 && stationaryPeriods >= 2;
    }
    
    /**
     * Analyze tactical pattern for an enemy
     */
    private void analyzeTacticalPattern(String enemyId, List<EnemyPositionSample> history, Bitmap screenshot) {
        if (history.size() < MIN_SAMPLES_FOR_PATTERN) {
            recognizedTacticalPatterns.put(enemyId, TACTIC_UNKNOWN);
            return;
        }
        
        // Get recent positions
        EnemyPositionSample latest = history.get(history.size() - 1);
        Point currentPos = new Point(latest.x, latest.y);
        
        // Screen center (approximate player position)
        Point screenCenter = new Point(screenshot.getWidth() / 2, screenshot.getHeight() / 2);
        
        // Calculate distance and angle to center
        float distanceToCenter = calculateDistance(currentPos, screenCenter);
        float angle = (float) Math.atan2(currentPos.y - screenCenter.y, currentPos.x - screenCenter.x);
        
        // Calculate average movement direction
        float avgDirectionX = 0;
        float avgDirectionY = 0;
        
        // Track if movement is oscillating (back and forth)
        int directionChanges = 0;
        float prevDx = 0;
        float prevDy = 0;
        
        for (int i = 1; i < history.size(); i++) {
            EnemyPositionSample curr = history.get(i);
            EnemyPositionSample prev = history.get(i - 1);
            
            float dx = curr.x - prev.x;
            float dy = curr.y - prev.y;
            
            // Check for direction change
            if (i > 1) {
                if ((prevDx * dx < 0) || (prevDy * dy < 0)) {
                    directionChanges++;
                }
            }
            
            prevDx = dx;
            prevDy = dy;
            
            avgDirectionX += dx;
            avgDirectionY += dy;
        }
        
        avgDirectionX /= (history.size() - 1);
        avgDirectionY /= (history.size() - 1);
        
        // Calculate if enemy is moving toward or away from center
        float dotProduct = avgDirectionX * (screenCenter.x - currentPos.x) + 
                          avgDirectionY * (screenCenter.y - currentPos.y);
        
        boolean movingTowardCenter = dotProduct > 0;
        boolean movingAwayFromCenter = dotProduct < 0;
        boolean isClose = distanceToCenter < (screenshot.getWidth() / 3);
        boolean isMediumDistance = distanceToCenter < (screenshot.getWidth() / 2) && !isClose;
        boolean isFar = distanceToCenter >= (screenshot.getWidth() / 2);
        
        // Check if the enemy's movement speed has changed dramatically
        boolean hasAccelerated = hasAccelerated(history);
        boolean hasDecelerated = hasDecelerated(history);
        
        // Check if the enemy is in formation with others (would require multi-enemy analysis)
        boolean inFormation = detectFormation(enemyId);
        
        // Get movement pattern
        String movementPattern = recognizedMovementPatterns.get(enemyId);
        if (movementPattern == null) {
            movementPattern = PATTERN_UNKNOWN;
        }
        
        // Determine tactical pattern with more advanced rules
        String tacticalPattern = TACTIC_UNKNOWN;
        
        // Start with movement pattern-based classification
        if (movementPattern.equals(PATTERN_STATIONARY)) {
            if (isPositionBehindCover(currentPos, screenshot)) {
                tacticalPattern = TACTIC_AMBUSH;
            } else if (isFar) {
                tacticalPattern = TACTIC_SUPPRESSIVE; // Likely providing covering fire
            } else {
                tacticalPattern = TACTIC_DEFENSIVE;
            }
        } else if (movementPattern.equals(PATTERN_PEEKING)) {
            if (isPositionBehindCover(currentPos, screenshot)) {
                tacticalPattern = TACTIC_AMBUSH;
            } else {
                tacticalPattern = TACTIC_DEFENSIVE;
            }
        } else if (movementPattern.equals(PATTERN_ERRATIC)) {
            if (isClose) {
                tacticalPattern = TACTIC_AGGRESSIVE; // Close-range erratic movement is aggressive
            } else {
                tacticalPattern = TACTIC_BAIT; // Trying to draw fire or attention
            }
        } else if (movementPattern.equals(PATTERN_CIRCLING)) {
            tacticalPattern = TACTIC_FLANKING; // Circling is a flanking maneuver
        } else if (movementPattern.equals(PATTERN_LINEAR)) {
            if (movingAwayFromCenter) {
                tacticalPattern = TACTIC_RETREAT; // Linear movement away is retreat
            } else if (movingTowardCenter) {
                tacticalPattern = TACTIC_AGGRESSIVE; // Linear movement toward is aggressive
            }
        }
        
        // If we couldn't determine based on movement pattern, use position and direction
        if (tacticalPattern.equals(TACTIC_UNKNOWN)) {
            if (movingTowardCenter && isClose) {
                tacticalPattern = TACTIC_AGGRESSIVE;
            } else if (!movingTowardCenter && !isPositionInFrontOfPlayer(currentPos, screenCenter, angle)) {
                tacticalPattern = TACTIC_FLANKING;
            } else if (movingTowardCenter && !isClose) {
                tacticalPattern = TACTIC_AGGRESSIVE;
            } else if (movingAwayFromCenter && hasAccelerated) {
                tacticalPattern = TACTIC_RETREAT;
            } else if (directionChanges > history.size() / 3 && !isClose) {
                tacticalPattern = TACTIC_BAIT;
            } else if (inFormation) {
                tacticalPattern = TACTIC_FORMATION;
            }
        }
        
        // Apply formation detection as override
        if (inFormation && !tacticalPattern.equals(TACTIC_RETREAT)) {
            tacticalPattern = TACTIC_FORMATION;
        }
        
        // Update the recognized tactical pattern
        recognizedTacticalPatterns.put(enemyId, tacticalPattern);
    }
    
    /**
     * Check if the enemy has accelerated significantly in recent samples
     */
    private boolean hasAccelerated(List<EnemyPositionSample> history) {
        if (history.size() < 4) return false;
        
        // Split history in half and compare speeds
        int midpoint = history.size() / 2;
        float earlySpeed = calculateAverageSpeed(history.subList(0, midpoint));
        float lateSpeed = calculateAverageSpeed(history.subList(midpoint, history.size()));
        
        // Check if speed increased by at least 50%
        return lateSpeed > earlySpeed * 1.5f;
    }
    
    /**
     * Check if the enemy has decelerated significantly in recent samples
     */
    private boolean hasDecelerated(List<EnemyPositionSample> history) {
        if (history.size() < 4) return false;
        
        // Split history in half and compare speeds
        int midpoint = history.size() / 2;
        float earlySpeed = calculateAverageSpeed(history.subList(0, midpoint));
        float lateSpeed = calculateAverageSpeed(history.subList(midpoint, history.size()));
        
        // Check if speed decreased by at least 50%
        return earlySpeed > lateSpeed * 1.5f;
    }
    
    /**
     * Calculate average movement speed from a list of samples
     */
    private float calculateAverageSpeed(List<EnemyPositionSample> samples) {
        if (samples.size() < 2) return 0;
        
        float totalDistance = 0;
        float totalTime = 0;
        
        for (int i = 1; i < samples.size(); i++) {
            EnemyPositionSample curr = samples.get(i);
            EnemyPositionSample prev = samples.get(i - 1);
            
            float distance = calculateDistance(
                    new Point(prev.x, prev.y),
                    new Point(curr.x, curr.y)
            );
            
            float timeSeconds = (curr.timestamp - prev.timestamp) / 1000.0f;
            if (timeSeconds > 0) {
                totalDistance += distance;
                totalTime += timeSeconds;
            }
        }
        
        return totalTime > 0 ? totalDistance / totalTime : 0;
    }
    
    /**
     * Detect if the enemy is in formation with other enemies
     */
    private boolean detectFormation(String targetEnemyId) {
        // Get the positions of all tracked enemies
        Map<String, Point> enemyPositions = new HashMap<>();
        
        for (Map.Entry<String, List<EnemyPositionSample>> entry : enemyPositionHistory.entrySet()) {
            String enemyId = entry.getKey();
            List<EnemyPositionSample> history = entry.getValue();
            
            if (!history.isEmpty()) {
                EnemyPositionSample lastSample = history.get(history.size() - 1);
                enemyPositions.put(enemyId, new Point(lastSample.x, lastSample.y));
            }
        }
        
        // We need at least 3 enemies for a formation
        if (enemyPositions.size() < 3) {
            return false;
        }
        
        // Get the target enemy position
        Point targetPos = enemyPositions.get(targetEnemyId);
        if (targetPos == null) {
            return false;
        }
        
        // Count enemies that are in a similar arrangement (similar distances)
        int formationCount = 0;
        float formationDistance = 200; // Reasonable distance for a formation
        
        for (Map.Entry<String, Point> entry : enemyPositions.entrySet()) {
            if (entry.getKey().equals(targetEnemyId)) {
                continue;
            }
            
            Point otherPos = entry.getValue();
            float distance = calculateDistance(targetPos, otherPos);
            
            if (distance < formationDistance) {
                formationCount++;
            }
        }
        
        // If at least 2 other enemies are in formation range, consider it a formation
        return formationCount >= 2;
    }
    
    /**
     * Check if a position is likely behind cover (simplified)
     */
    private boolean isPositionBehindCover(Point position, Bitmap screenshot) {
        // This is a simplified approximation that would need game-specific implementation
        // In a real implementation, this would analyze the game environment
        
        // Check if position is near the edge of the screen, which often indicates cover
        int margin = screenshot.getWidth() / 10;
        return position.x < margin || 
              position.x > screenshot.getWidth() - margin ||
              position.y < margin ||
              position.y > screenshot.getHeight() - margin;
    }
    
    /**
     * Check if a position is in front of the player
     */
    private boolean isPositionInFrontOfPlayer(Point position, Point playerPos, float angle) {
        // Define a frontal arc (120 degrees)
        float arcHalfWidth = (float) (Math.PI / 3);
        
        // Check if enemy is within this arc
        float angleDiff = Math.abs(angle);
        return angleDiff < arcHalfWidth;
    }
    
    /**
     * Calculate threat level for an enemy based on pattern and position
     */
    private void calculateThreatLevel(String enemyId, List<EnemyPositionSample> history) {
        if (history.size() < 2) {
            enemyThreatLevels.put(enemyId, 0.5f); // Default medium threat
            return;
        }
        
        // Get patterns
        String movementPattern = recognizedMovementPatterns.get(enemyId);
        String tacticalPattern = recognizedTacticalPatterns.get(enemyId);
        
        if (movementPattern == null) movementPattern = PATTERN_UNKNOWN;
        if (tacticalPattern == null) tacticalPattern = TACTIC_UNKNOWN;
        
        // Base threat by tactical pattern (expanded with new patterns)
        float threatLevel = 0.5f; // Medium by default
        
        switch (tacticalPattern) {
            case TACTIC_AGGRESSIVE:
                threatLevel = 0.9f; // Very high threat
                break;
            case TACTIC_AMBUSH:
                threatLevel = 0.85f; // High threat
                break;
            case TACTIC_FLANKING:
                threatLevel = 0.8f; // High threat
                break;
            case TACTIC_SUPPRESSIVE:
                threatLevel = 0.75f; // Medium-high threat
                break;
            case TACTIC_FORMATION:
                threatLevel = 0.7f; // Medium-high threat (coordinated enemies)
                break;
            case TACTIC_BAIT:
                threatLevel = 0.5f; // Medium threat (potential trap)
                break;
            case TACTIC_DEFENSIVE:
                threatLevel = 0.4f; // Medium-low threat
                break;
            case TACTIC_RETREAT:
                threatLevel = 0.2f; // Low threat (enemy is retreating)
                break;
            default:
                threatLevel = 0.5f; // Medium threat for unknown
                break;
        }
        
        // Adjust by movement pattern (expanded)
        switch (movementPattern) {
            case PATTERN_STATIONARY:
                // Stationary enemies are less threatening unless ambushing/suppressive
                if (!tacticalPattern.equals(TACTIC_AMBUSH) && !tacticalPattern.equals(TACTIC_SUPPRESSIVE)) {
                    threatLevel *= 0.8f;
                }
                break;
            case PATTERN_ERRATIC:
                // Erratic movement is harder to predict
                threatLevel *= 1.2f;
                break;
            case PATTERN_STRAFING:
                // Strafing enemies are often actively engaging
                threatLevel *= 1.1f;
                break;
            case PATTERN_CIRCLING:
                // Circling indicates the enemy is trying to get a better position
                threatLevel *= 1.1f;
                break;
            case PATTERN_PEEKING:
                // Peeking can be particularly dangerous in FPS games
                if (tacticalPattern.equals(TACTIC_AMBUSH)) {
                    threatLevel *= 1.3f; // Peeking from ambush is very threatening
                } else {
                    threatLevel *= 1.1f;
                }
                break;
        }
        
        // Calculate speed-based threat adjustment
        float speedThreatFactor = calculateSpeedThreatFactor(history);
        threatLevel *= speedThreatFactor;
        
        // Calculate size-based threat adjustment (larger enemies might be more dangerous or closer)
        float sizeThreatFactor = calculateSizeThreatFactor(history);
        threatLevel *= sizeThreatFactor;
        
        // Calculate recency factor (more recent enemies are more threatening)
        EnemyPositionSample latest = history.get(history.size() - 1);
        long currentTime = System.currentTimeMillis();
        float recencyFactor = 1.0f - Math.min(1.0f, (currentTime - latest.timestamp) / (float) MAX_PATTERN_AGE_MS);
        
        // Calculate proximity (closer enemies are more threatening)
        float proximityFactor = 1.0f;
        if (history.size() > 0) {
            // Distance from center of screen (assumed to be player position)
            EnemyPositionSample currentSample = history.get(history.size() - 1);
            
            // Approximate screen dimensions for calculation
            int screenWidth = 1080; // Typical phone width
            int screenHeight = 1920; // Typical phone height
            
            // Calculate distance from center
            Point center = new Point(screenWidth / 2, screenHeight / 2);
            Point enemyPos = new Point(currentSample.x, currentSample.y);
            float distance = calculateDistance(enemyPos, center);
            
            // Normalize distance (0 = at center, 1 = at screen edge)
            float maxDistance = (float) Math.sqrt(screenWidth * screenWidth / 4 + screenHeight * screenHeight / 4);
            float normalizedDistance = Math.min(1.0f, distance / maxDistance);
            
            // Closer enemies are more threatening (inverse relationship)
            proximityFactor = 2.0f - normalizedDistance; // Range: 1.0 to 2.0
        }
        
        // Check for other nearby enemies (group threat)
        float groupThreatFactor = calculateGroupThreatFactor(enemyId);
        
        // Adjust with line of sight (if we can determine it)
        float lineOfSightFactor = isInLineOfSight(history) ? 1.2f : 1.0f;
        
        // Combine all factors
        float finalThreat = threatLevel * recencyFactor * proximityFactor * groupThreatFactor * lineOfSightFactor;
        
        // Ensure threat is within bounds
        finalThreat = Math.min(1.0f, Math.max(0.0f, finalThreat));
        
        // Update threat level
        enemyThreatLevels.put(enemyId, finalThreat);
        
        // Log high threat enemies
        if (finalThreat > 0.8f) {
            Log.d(TAG, "High threat enemy detected: " + enemyId + 
                  " (Threat: " + String.format("%.2f", finalThreat) + 
                  ", Pattern: " + movementPattern + 
                  ", Tactic: " + tacticalPattern + ")");
        }
    }
    
    /**
     * Calculate threat factor based on enemy movement speed
     */
    private float calculateSpeedThreatFactor(List<EnemyPositionSample> history) {
        if (history.size() < 3) return 1.0f;
        
        // Calculate average speed
        float avgSpeed = calculateAverageSpeed(history);
        
        // Very slow or very fast movements can indicate different threat levels
        if (avgSpeed < 10) {
            return 0.9f; // Slow-moving enemies might be less threatening
        } else if (avgSpeed > 200) {
            return 1.3f; // Fast-moving enemies are more threatening
        } else if (avgSpeed > 100) {
            return 1.2f; // Moderately fast enemies
        } else {
            return 1.0f; // Normal speed
        }
    }
    
    /**
     * Calculate threat factor based on enemy size
     */
    private float calculateSizeThreatFactor(List<EnemyPositionSample> history) {
        if (history.isEmpty()) return 1.0f;
        
        // Get latest sample for current size
        EnemyPositionSample latest = history.get(history.size() - 1);
        
        // Calculate area
        float area = latest.width * latest.height;
        
        // Normalize area based on screen size (assuming 1080x1920 screen)
        float screenArea = 1080 * 1920;
        float normalizedArea = area / screenArea;
        
        // Larger enemies (closer to screen) are more threatening
        if (normalizedArea > 0.1f) {
            return 1.5f; // Very large (close) enemy
        } else if (normalizedArea > 0.05f) {
            return 1.3f; // Large enemy
        } else if (normalizedArea > 0.02f) {
            return 1.2f; // Medium-sized enemy
        } else {
            return 1.0f; // Small (distant) enemy
        }
    }
    
    /**
     * Calculate threat factor based on nearby enemies (group threat)
     */
    private float calculateGroupThreatFactor(String targetEnemyId) {
        // Count enemies in close proximity to the target
        int nearbyEnemies = 0;
        Point targetPos = null;
        
        // Get target position
        List<EnemyPositionSample> targetHistory = enemyPositionHistory.get(targetEnemyId);
        if (targetHistory != null && !targetHistory.isEmpty()) {
            EnemyPositionSample latest = targetHistory.get(targetHistory.size() - 1);
            targetPos = new Point(latest.x, latest.y);
        } else {
            return 1.0f; // No position data
        }
        
        // Check all other enemies
        for (Map.Entry<String, List<EnemyPositionSample>> entry : enemyPositionHistory.entrySet()) {
            String enemyId = entry.getKey();
            if (enemyId.equals(targetEnemyId)) continue;
            
            List<EnemyPositionSample> history = entry.getValue();
            if (history.isEmpty()) continue;
            
            EnemyPositionSample latest = history.get(history.size() - 1);
            Point otherPos = new Point(latest.x, latest.y);
            
            // Check if enemy is nearby (within 200 pixels)
            if (calculateDistance(targetPos, otherPos) < 200) {
                nearbyEnemies++;
            }
        }
        
        // Adjust threat based on group size
        if (nearbyEnemies >= 3) {
            return 1.5f; // Large group
        } else if (nearbyEnemies == 2) {
            return 1.3f; // Small group
        } else if (nearbyEnemies == 1) {
            return 1.1f; // Pair
        } else {
            return 1.0f; // Solo enemy
        }
    }
    
    /**
     * Check if enemy is in direct line of sight with player
     */
    private boolean isInLineOfSight(List<EnemyPositionSample> history) {
        if (history.isEmpty()) return false;
        
        // In a real implementation, this would use raycasting to check for obstacles
        // For this simplified version, we'll use a proxy calculation
        
        // Get the latest position
        EnemyPositionSample latest = history.get(history.size() - 1);
        
        // Calculate distance from screen center
        int screenCenterX = 1080 / 2; // Assuming 1080p width
        int screenCenterY = 1920 / 2; // Assuming 1920p height
        
        // Distance from center
        float distanceFromCenter = (float) Math.sqrt(
                Math.pow(latest.x - screenCenterX, 2) + 
                Math.pow(latest.y - screenCenterY, 2));
        
        // Assume enemies near the center of the screen are more likely in line of sight
        // and enemies near the edges might be partially occluded
        float centerThreshold = Math.min(1080, 1920) / 3.0f;
        
        return distanceFromCenter < centerThreshold;
    }
    
    /**
     * Clean up old data to prevent memory issues
     */
    private void cleanupStaleData(long currentTime, List<String> currentEnemyIds) {
        // Remove old samples
        for (List<EnemyPositionSample> history : enemyPositionHistory.values()) {
            while (!history.isEmpty() && 
                  (currentTime - history.get(0).timestamp) > MAX_PATTERN_AGE_MS) {
                history.remove(0);
            }
        }
        
        // Remove tracking for enemies that haven't been seen recently
        List<String> idsToRemove = new ArrayList<>();
        
        for (Map.Entry<String, List<EnemyPositionSample>> entry : enemyPositionHistory.entrySet()) {
            String enemyId = entry.getKey();
            List<EnemyPositionSample> history = entry.getValue();
            
            // If enemy not in current frame and last seen too long ago, remove it
            if (!currentEnemyIds.contains(enemyId) && !history.isEmpty()) {
                long lastSeen = history.get(history.size() - 1).timestamp;
                if (currentTime - lastSeen > MAX_PATTERN_AGE_MS) {
                    idsToRemove.add(enemyId);
                }
            }
            
            // Also remove if history is empty
            if (history.isEmpty()) {
                idsToRemove.add(enemyId);
            }
        }
        
        // Perform removal
        for (String idToRemove : idsToRemove) {
            enemyPositionHistory.remove(idToRemove);
            recognizedMovementPatterns.remove(idToRemove);
            recognizedTacticalPatterns.remove(idToRemove);
            enemyThreatLevels.remove(idToRemove);
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
     * Log pattern information for debugging
     */
    private void logPatternInfo() {
        StringBuilder sb = new StringBuilder("Enemy Patterns: ");
        for (Map.Entry<String, String> entry : recognizedMovementPatterns.entrySet()) {
            String enemyId = entry.getKey();
            String movementPattern = entry.getValue();
            String tacticalPattern = recognizedTacticalPatterns.get(enemyId);
            Float threatLevel = enemyThreatLevels.get(enemyId);
            
            if (tacticalPattern == null) tacticalPattern = TACTIC_UNKNOWN;
            if (threatLevel == null) threatLevel = 0.5f;
            
            sb.append(enemyId).append("=[")
              .append(movementPattern).append(",")
              .append(tacticalPattern).append(",")
              .append(String.format("%.2f", threatLevel)).append("] ");
        }
        
        if (recognizedMovementPatterns.size() > 0) {
            Log.d(TAG, sb.toString());
        }
    }
    
    /**
     * Get detected movement pattern for an enemy
     * @param enemyId The enemy identifier
     * @return The movement pattern, or PATTERN_UNKNOWN if not recognized
     */
    public String getMovementPattern(String enemyId) {
        String pattern = recognizedMovementPatterns.get(enemyId);
        return pattern != null ? pattern : PATTERN_UNKNOWN;
    }
    
    /**
     * Get detected tactical pattern for an enemy
     * @param enemyId The enemy identifier
     * @return The tactical pattern, or TACTIC_UNKNOWN if not recognized
     */
    public String getTacticalPattern(String enemyId) {
        String pattern = recognizedTacticalPatterns.get(enemyId);
        return pattern != null ? pattern : TACTIC_UNKNOWN;
    }
    
    /**
     * Get calculated threat level for an enemy
     * @param enemyId The enemy identifier
     * @return Threat level from 0.0 (no threat) to 1.0 (maximum threat)
     */
    public float getThreatLevel(String enemyId) {
        Float level = enemyThreatLevels.get(enemyId);
        return level != null ? level : 0.5f;
    }
    
    /**
     * Get the most threatening enemy
     * @return ID of the most threatening enemy, or null if none
     */
    public String getMostThreateningEnemyId() {
        String mostThreatening = null;
        float highestThreat = 0;
        
        for (Map.Entry<String, Float> entry : enemyThreatLevels.entrySet()) {
            if (entry.getValue() > highestThreat) {
                highestThreat = entry.getValue();
                mostThreatening = entry.getKey();
            }
        }
        
        return mostThreatening;
    }
    
    /**
     * Get success rate of pattern recognition
     * @return Percentage of successful pattern recognitions
     */
    public float getPatternRecognitionSuccessRate() {
        if (totalPatternsAnalyzed == 0) {
            return 0;
        }
        return (float) successfulPatternRecognitions / totalPatternsAnalyzed;
    }
    
    /**
     * Sample for storing enemy position with timestamp
     */
    private static class EnemyPositionSample {
        public final int x;           // X position
        public final int y;           // Y position
        public final int width;       // Bounding box width
        public final int height;      // Bounding box height
        public final long timestamp;  // Time when the sample was taken
        
        public EnemyPositionSample(int x, int y, int width, int height, long timestamp) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.timestamp = timestamp;
        }
    }
}
