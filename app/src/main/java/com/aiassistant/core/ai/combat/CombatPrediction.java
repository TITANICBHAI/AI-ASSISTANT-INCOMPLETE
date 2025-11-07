package com.aiassistant.core.ai.combat;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.aiassistant.data.models.DetectedEnemy;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.ScreenActionEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for predicting combat outcomes and optimal actions
 */
public class CombatPrediction {
    
    private static final String TAG = "CombatPrediction";
    
    private final Context context;
    private final CombatPatternRecognition patternRecognition;
    private float playerSkillLevel;
    private float aggressiveness;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public CombatPrediction(Context context) {
        this.context = context.getApplicationContext();
        this.patternRecognition = new CombatPatternRecognition(context);
        this.playerSkillLevel = 0.5f;
        this.aggressiveness = 0.5f;
    }
    
    /**
     * Process a game state to update internal models
     * 
     * @param gameState The game state
     */
    public void processGameState(GameState gameState) {
        if (gameState == null) {
            return;
        }
        
        // Update pattern recognition
        patternRecognition.processEnemies(gameState);
    }
    
    /**
     * Predict the optimal action to take
     * 
     * @param gameState The current game state
     * @return The recommended action
     */
    public ScreenActionEntity predictOptimalAction(GameState gameState) {
        if (gameState == null || !gameState.isInCombat() || gameState.getEnemies().isEmpty()) {
            return null;
        }
        
        // If there's a clear, immediate threat, target it
        DetectedEnemy mostDangerousEnemy = findMostDangerousEnemy(gameState);
        if (mostDangerousEnemy != null && mostDangerousEnemy.getThreatLevel() > 0.7f) {
            // Get aim point accounting for enemy movement
            float[] aimPoint = getOptimalAimPoint(mostDangerousEnemy);
            if (aimPoint != null) {
                return new ScreenActionEntity(ScreenActionEntity.ACTION_TAP, aimPoint[0], aimPoint[1]);
            }
        }
        
        // Consider the overall combat situation
        CombatSituation situation = evaluateCombatSituation(gameState);
        
        switch (situation) {
            case ADVANTAGEOUS:
                // Take aggressive action
                return generateAggressiveAction(gameState);
                
            case NEUTRAL:
                // Take balanced action
                return generateBalancedAction(gameState);
                
            case DISADVANTAGEOUS:
                // Take defensive action
                return generateDefensiveAction(gameState);
                
            case CRITICAL:
                // Take survival action
                return generateSurvivalAction(gameState);
                
            default:
                return null;
        }
    }
    
    /**
     * Enum for combat situations
     */
    public enum CombatSituation {
        ADVANTAGEOUS,  // We have a clear advantage
        NEUTRAL,       // Even fight
        DISADVANTAGEOUS, // Enemy has an advantage
        CRITICAL       // We're in serious danger
    }
    
    /**
     * Find the most dangerous enemy
     * 
     * @param gameState The game state
     * @return The most dangerous enemy or null
     */
    private DetectedEnemy findMostDangerousEnemy(GameState gameState) {
        if (gameState == null || gameState.getEnemies() == null || gameState.getEnemies().isEmpty()) {
            return null;
        }
        
        DetectedEnemy mostDangerous = null;
        float highestThreat = 0;
        
        for (DetectedEnemy enemy : gameState.getEnemies()) {
            float threat = calculateEnemyThreat(enemy, gameState);
            if (threat > highestThreat) {
                highestThreat = threat;
                mostDangerous = enemy;
            }
        }
        
        return mostDangerous;
    }
    
    /**
     * Calculate the threat level of an enemy
     * 
     * @param enemy The enemy
     * @param gameState The game state
     * @return The threat level (0-1)
     */
    private float calculateEnemyThreat(DetectedEnemy enemy, GameState gameState) {
        if (enemy == null) {
            return 0;
        }
        
        // Factors affecting threat:
        // 1. Distance (closer = more dangerous)
        // 2. Size (larger = more dangerous)
        // 3. Visibility
        // 4. Movement pattern
        // 5. Player health
        
        // Distance factor
        float distanceFactor = 1.0f - Math.min(1.0f, enemy.getDistance() / 1000.0f);
        
        // Size factor
        float area = enemy.getWidth() * enemy.getHeight();
        float screenArea = gameState.getScreenWidth() * gameState.getScreenHeight();
        float sizeFactor = Math.min(1.0f, area / (screenArea * 0.05f)); // Size relative to 5% of screen
        
        // Visibility factor
        float visibilityFactor = enemy.isVisible() ? 1.0f : 0.5f;
        
        // Pattern factor from recognition
        float patternFactor = patternRecognition.calculateDangerLevel(enemy);
        
        // Health factor (lower health = perceive threats as greater)
        float healthFactor = 1.0f - (gameState.getPlayerHealth() / 100.0f);
        
        // Combine factors (weighted)
        float threat = (0.35f * distanceFactor) + 
                      (0.2f * sizeFactor) + 
                      (0.15f * visibilityFactor) + 
                      (0.2f * patternFactor) + 
                      (0.1f * healthFactor);
        
        // Update enemy's threat level for future reference
        enemy.setThreatLevel(threat);
        
        return threat;
    }
    
    /**
     * Get the optimal aim point for an enemy
     * 
     * @param enemy The enemy
     * @return The optimal aim coordinates [x, y] or null
     */
    private float[] getOptimalAimPoint(DetectedEnemy enemy) {
        if (enemy == null) {
            return null;
        }
        
        // For stationary enemies, aim at center
        if (Math.abs(enemy.getVelocityX()) < 1 && Math.abs(enemy.getVelocityY()) < 1) {
            return new float[]{enemy.getCenterX(), enemy.getCenterY()};
        }
        
        // For moving enemies, predict future position
        // Simple prediction: current position + velocity * time
        // Assume time = 250ms (reasonable for human reaction + bullet travel)
        float predictionTimeMs = 250;
        float predictionTimeSec = predictionTimeMs / 1000.0f;
        
        float predictedX = enemy.getCenterX() + (enemy.getVelocityX() * predictionTimeSec);
        float predictedY = enemy.getCenterY() + (enemy.getVelocityY() * predictionTimeSec);
        
        return new float[]{predictedX, predictedY};
    }
    
    /**
     * Evaluate the overall combat situation
     * 
     * @param gameState The game state
     * @return The combat situation
     */
    private CombatSituation evaluateCombatSituation(GameState gameState) {
        if (gameState == null) {
            return CombatSituation.NEUTRAL;
        }
        
        // Factors to consider:
        // 1. Player health
        // 2. Number of enemies
        // 3. Enemy threat levels
        // 4. Available ammo
        // 5. Cover availability
        
        // Health factor (0-1)
        float healthFactor = gameState.getPlayerHealth() / 100.0f;
        
        // Enemies factor (more enemies = worse situation)
        List<DetectedEnemy> enemies = gameState.getEnemies();
        int enemyCount = enemies != null ? enemies.size() : 0;
        float enemyCountFactor = Math.min(1.0f, enemyCount / 5.0f); // 5+ enemies = max
        
        // Total threat factor
        float threatScore = 0;
        if (enemies != null) {
            for (DetectedEnemy enemy : enemies) {
                threatScore += enemy.getThreatLevel() / 10.0f;
            }
        }
        float threatFactor = Math.min(1.0f, threatScore);
        
        // Ammo factor
        float ammoScore = gameState.getPlayerAmmo() / 100.0f;
        
        // Calculate overall situation score (0-1, higher = better situation)
        float situationScore = (0.4f * healthFactor) + 
                              (0.1f * (1.0f - enemyCountFactor)) + 
                              (0.3f * (1.0f - threatFactor)) + 
                              (0.2f * ammoScore);
        
        // Map to situation enum
        if (situationScore > 0.75f) {
            return CombatSituation.ADVANTAGEOUS;
        } else if (situationScore > 0.5f) {
            return CombatSituation.NEUTRAL;
        } else if (situationScore > 0.25f) {
            return CombatSituation.DISADVANTAGEOUS;
        } else {
            return CombatSituation.CRITICAL;
        }
    }
    
    /**
     * Generate an aggressive action (when in advantage)
     * 
     * @param gameState The game state
     * @return The action or null
     */
    private ScreenActionEntity generateAggressiveAction(GameState gameState) {
        // When in advantage, focus on highest threat enemy
        DetectedEnemy target = findMostDangerousEnemy(gameState);
        
        if (target != null) {
            float[] aimPoint = getOptimalAimPoint(target);
            if (aimPoint != null) {
                return new ScreenActionEntity(ScreenActionEntity.ACTION_TAP, aimPoint[0], aimPoint[1]);
            }
        }
        
        return null;
    }
    
    /**
     * Generate a balanced action (neutral situation)
     * 
     * @param gameState The game state
     * @return The action or null
     */
    private ScreenActionEntity generateBalancedAction(GameState gameState) {
        // In neutral situation, still target enemies but be more cautious
        List<DetectedEnemy> enemies = gameState.getEnemies();
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }
        
        float totalThreat = 0;
        for (DetectedEnemy enemy : enemies) {
            totalThreat += enemy.getThreatLevel();
        }
        
        // If total threat is high enough, target most dangerous
        if (totalThreat > 1.0f) {
            return generateAggressiveAction(gameState);
        }
        
        // Otherwise, take defensive position
        return generateDefensiveAction(gameState);
    }
    
    /**
     * Generate a defensive action (disadvantageous situation)
     * 
     * @param gameState The game state
     * @return The action or null
     */
    private ScreenActionEntity generateDefensiveAction(GameState gameState) {
        List<Rect> coverPositions = detectNearestCover(gameState);
        
        if (!coverPositions.isEmpty()) {
            Rect nearestCover = coverPositions.get(0);
            float coverCenterX = nearestCover.centerX();
            float coverCenterY = nearestCover.centerY();
            
            float screenCenterX = gameState.getScreenWidth() / 2.0f;
            float screenBottomY = gameState.getScreenHeight() * 0.8f;
            
            float swipeEndX = screenCenterX + (coverCenterX - screenCenterX) * 0.5f;
            float swipeEndY = screenBottomY + (coverCenterY - screenBottomY) * 0.5f;
            
            return new ScreenActionEntity(ScreenActionEntity.ACTION_SWIPE, 
                screenCenterX, screenBottomY, swipeEndX, swipeEndY, 400);
        }
        
        DetectedEnemy mostDangerous = findMostDangerousEnemy(gameState);
        
        if (mostDangerous != null) {
            // Calculate direction from enemy to player (assumed center bottom)
            float screenCenterX = gameState.getScreenWidth() / 2.0f;
            float screenBottomY = gameState.getScreenHeight() * 0.8f;
            
            float dx = screenCenterX - mostDangerous.getCenterX();
            float dy = screenBottomY - mostDangerous.getCenterY();
            
            // Normalize
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }
            
            // Move perpendicular (90 degrees) to dodge
            float perpX = -dy;
            float perpY = dx;
            
            // Determine starting and ending points for swipe
            float startX = screenCenterX;
            float startY = screenBottomY;
            float endX = startX + perpX * 200; // Swipe distance
            float endY = startY + perpY * 200;
            
            return new ScreenActionEntity(ScreenActionEntity.ACTION_SWIPE, startX, startY, endX, endY, 300);
        }
        
        return null;
    }
    
    /**
     * Generate a survival action (critical situation)
     * 
     * @param gameState The game state
     * @return The action or null
     */
    private ScreenActionEntity generateSurvivalAction(GameState gameState) {
        // In critical situation, prioritize survival
        // Example: Run away from all enemies
        
        List<DetectedEnemy> enemies = gameState.getEnemies();
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }
        
        // Calculate average enemy position
        float avgX = 0;
        float avgY = 0;
        int count = 0;
        
        for (DetectedEnemy enemy : enemies) {
            avgX += enemy.getCenterX();
            avgY += enemy.getCenterY();
            count++;
        }
        
        if (count > 0) {
            avgX /= count;
            avgY /= count;
            
            // Move away from average enemy position
            float screenCenterX = gameState.getScreenWidth() / 2.0f;
            float screenBottomY = gameState.getScreenHeight() * 0.8f;
            
            // Direction from average enemy position to player
            float dx = screenCenterX - avgX;
            float dy = screenBottomY - avgY;
            
            // Normalize
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }
            
            // Determine starting and ending points for swipe (run away)
            float startX = screenCenterX;
            float startY = screenBottomY;
            float endX = startX + dx * 400; // Long swipe to run
            float endY = startY + dy * 400;
            
            return new ScreenActionEntity(ScreenActionEntity.ACTION_SWIPE, startX, startY, endX, endY, 500);
        }
        
        return null;
    }
    
    /**
     * Predict future game state
     * 
     * @param currentState The current state
     * @param timeHorizonMs The time horizon in milliseconds
     * @return The predicted future state
     */
    public GameState predictFutureState(GameState currentState, long timeHorizonMs) {
        if (currentState == null) {
            return null;
        }
        
        // Create a new state based on current
        GameState futureState = new GameState();
        futureState.setGameId(currentState.getGameId());
        futureState.setScreenWidth(currentState.getScreenWidth());
        futureState.setScreenHeight(currentState.getScreenHeight());
        futureState.setTimestamp(currentState.getTimestamp() + timeHorizonMs);
        futureState.setInCombat(currentState.isInCombat());
        futureState.setPlayerHealth(currentState.getPlayerHealth());
        futureState.setAmmoCount(currentState.getAmmoCount());
        
        // Predict future enemy positions
        List<DetectedEnemy> currentEnemies = currentState.getEnemies();
        List<DetectedEnemy> futureEnemies = new ArrayList<>();
        
        if (currentEnemies != null) {
            float timeHorizonSec = timeHorizonMs / 1000.0f;
            
            for (DetectedEnemy enemy : currentEnemies) {
                DetectedEnemy futureEnemy = new DetectedEnemy();
                
                // Copy basic properties
                futureEnemy.setId(enemy.getId());
                futureEnemy.setEnemyType(enemy.getEnemyType());
                futureEnemy.setConfidence(enemy.getConfidence() * 0.9f); // Decrease confidence over time
                futureEnemy.setVisible(enemy.isVisible());
                futureEnemy.setThreatLevel(enemy.getThreatLevel());
                futureEnemy.setDistanceEstimate(enemy.getDistanceEstimate());
                futureEnemy.setVelocityX(enemy.getVelocityX());
                futureEnemy.setVelocityY(enemy.getVelocityY());
                
                // Predict position
                Rect predictedPosition = enemy.predictPosition(timeHorizonSec);
                futureEnemy.setBoundingBox(predictedPosition);
                
                futureEnemies.add(futureEnemy);
            }
        }
        
        futureState.setEnemies(futureEnemies);
        
        return futureState;
    }
    
    // Getters and setters
    
    public float getPlayerSkillLevel() {
        return playerSkillLevel;
    }
    
    public void setPlayerSkillLevel(float playerSkillLevel) {
        this.playerSkillLevel = Math.max(0, Math.min(1, playerSkillLevel));
    }
    
    public float getAggressiveness() {
        return aggressiveness;
    }
    
    public void setAggressiveness(float aggressiveness) {
        this.aggressiveness = Math.max(0, Math.min(1, aggressiveness));
    }
    
    /**
     * Detect nearest cover positions in the game state
     * 
     * @param gameState The current game state
     * @return List of Rect objects representing cover positions
     */
    private List<Rect> detectNearestCover(GameState gameState) {
        List<Rect> coverPositions = new ArrayList<>();
        
        if (gameState == null) {
            return coverPositions;
        }
        
        int screenWidth = gameState.getScreenWidth();
        int screenHeight = gameState.getScreenHeight();
        
        List<DetectedEnemy> enemies = gameState.getEnemies();
        if (enemies == null || enemies.isEmpty()) {
            return coverPositions;
        }
        
        float avgEnemyX = 0;
        float avgEnemyY = 0;
        int enemyCount = 0;
        
        for (DetectedEnemy enemy : enemies) {
            avgEnemyX += enemy.getCenterX();
            avgEnemyY += enemy.getCenterY();
            enemyCount++;
        }
        
        if (enemyCount > 0) {
            avgEnemyX /= enemyCount;
            avgEnemyY /= enemyCount;
        }
        
        int gridSize = 6;
        int cellWidth = screenWidth / gridSize;
        int cellHeight = screenHeight / gridSize;
        
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int left = col * cellWidth;
                int top = row * cellHeight;
                int right = left + cellWidth;
                int bottom = top + cellHeight;
                
                Rect cellRect = new Rect(left, top, right, bottom);
                float cellCenterX = cellRect.centerX();
                float cellCenterY = cellRect.centerY();
                
                boolean hasCover = false;
                
                if (row < gridSize / 2) {
                    hasCover = true;
                }
                
                if (col == 0 || col == gridSize - 1) {
                    hasCover = true;
                }
                
                boolean hasEnemy = false;
                for (DetectedEnemy enemy : enemies) {
                    Rect enemyRect = new Rect(
                        (int)(enemy.getCenterX() - enemy.getWidth()/2),
                        (int)(enemy.getCenterY() - enemy.getHeight()/2),
                        (int)(enemy.getCenterX() + enemy.getWidth()/2),
                        (int)(enemy.getCenterY() + enemy.getHeight()/2)
                    );
                    
                    if (Rect.intersects(cellRect, enemyRect)) {
                        hasEnemy = true;
                        break;
                    }
                }
                
                if (hasCover && !hasEnemy) {
                    float distanceToEnemy = (float) Math.sqrt(
                        Math.pow(cellCenterX - avgEnemyX, 2) + 
                        Math.pow(cellCenterY - avgEnemyY, 2)
                    );
                    
                    if (distanceToEnemy > screenWidth * 0.2f) {
                        coverPositions.add(cellRect);
                    }
                }
            }
        }
        
        coverPositions.sort((r1, r2) -> {
            float playerX = screenWidth / 2.0f;
            float playerY = screenHeight * 0.8f;
            
            float dist1 = (float) Math.sqrt(
                Math.pow(r1.centerX() - playerX, 2) + 
                Math.pow(r1.centerY() - playerY, 2)
            );
            
            float dist2 = (float) Math.sqrt(
                Math.pow(r2.centerX() - playerX, 2) + 
                Math.pow(r2.centerY() - playerY, 2)
            );
            
            return Float.compare(dist1, dist2);
        });
        
        return coverPositions;
    }
}
