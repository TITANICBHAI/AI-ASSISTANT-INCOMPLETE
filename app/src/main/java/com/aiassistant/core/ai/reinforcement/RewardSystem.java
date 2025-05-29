package com.aiassistant.core.ai.reinforcement;

import android.content.Context;
import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;

/**
 * Reward system for reinforcement learning
 */
public class RewardSystem {
    
    private static final String TAG = "RewardSystem";
    
    private final Context context;
    
    // Reward coefficients
    private float healthRewardCoef = 1.0f;
    private float enemyDefeatRewardCoef = 1.0f;
    private float survivalRewardCoef = 0.05f;
    private float accuracyRewardCoef = 0.5f;
    private float resourceRewardCoef = 0.2f;
    private float objectiveRewardCoef = 1.0f;
    private float explicitFeedbackCoef = 2.0f;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public RewardSystem(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Calculate reward for transition from one state to another
     * 
     * @param previousState The previous state
     * @param action The action taken
     * @param currentState The current state
     * @param explicitFeedback Explicit feedback from the user (-1 to 1)
     * @return The reward
     */
    public float calculateReward(GameState previousState, AIAction action, 
                                GameState currentState, float explicitFeedback) {
        if (previousState == null || currentState == null) {
            return 0;
        }
        
        float totalReward = 0;
        
        // Health-based reward
        float healthReward = calculateHealthReward(previousState, currentState);
        totalReward += healthRewardCoef * healthReward;
        
        // Enemy defeat reward
        float enemyDefeatReward = calculateEnemyDefeatReward(previousState, currentState);
        totalReward += enemyDefeatRewardCoef * enemyDefeatReward;
        
        // Survival reward (small reward for staying alive)
        totalReward += survivalRewardCoef;
        
        // Accuracy reward (for targeting actions)
        float accuracyReward = calculateAccuracyReward(action, currentState);
        totalReward += accuracyRewardCoef * accuracyReward;
        
        // Resource management reward (ammo, items, etc.)
        float resourceReward = calculateResourceReward(previousState, currentState);
        totalReward += resourceRewardCoef * resourceReward;
        
        // Objective progress reward
        float objectiveReward = calculateObjectiveReward(previousState, currentState);
        totalReward += objectiveRewardCoef * objectiveReward;
        
        // Add explicit user feedback if provided
        if (explicitFeedback != 0) {
            totalReward += explicitFeedbackCoef * explicitFeedback;
            Log.d(TAG, "Added explicit feedback: " + explicitFeedback);
        }
        
        return totalReward;
    }
    
    /**
     * Calculate health-based reward
     * 
     * @param previousState The previous state
     * @param currentState The current state
     * @return The reward
     */
    private float calculateHealthReward(GameState previousState, GameState currentState) {
        int prevHealth = previousState.getPlayerHealth();
        int currHealth = currentState.getPlayerHealth();
        
        // Reward for health gain, penalty for health loss
        return (currHealth - prevHealth) / 100.0f;
    }
    
    /**
     * Calculate reward for defeating enemies
     * 
     * @param previousState The previous state
     * @param currentState The current state
     * @return The reward
     */
    private float calculateEnemyDefeatReward(GameState previousState, GameState currentState) {
        // Count visible enemies in each state
        int prevEnemyCount = 0;
        int currEnemyCount = 0;
        
        if (previousState.getEnemies() != null) {
            for (com.aiassistant.data.models.DetectedEnemy enemy : previousState.getEnemies()) {
                if (enemy.isVisible()) {
                    prevEnemyCount++;
                }
            }
        }
        
        if (currentState.getEnemies() != null) {
            for (com.aiassistant.data.models.DetectedEnemy enemy : currentState.getEnemies()) {
                if (enemy.isVisible()) {
                    currEnemyCount++;
                }
            }
        }
        
        // Reward for reducing enemy count
        return (prevEnemyCount - currEnemyCount) * 0.5f;
    }
    
    /**
     * Calculate accuracy reward
     * 
     * @param action The action
     * @param currentState The current state
     * @return The reward
     */
    private float calculateAccuracyReward(AIAction action, GameState currentState) {
        if (action == null || currentState == null || currentState.getEnemies() == null) {
            return 0;
        }
        
        // For tap actions, check if they were on or near an enemy
        if (action.getActionType() == com.aiassistant.data.models.ScreenActionEntity.ACTION_TAP) {
            float x = action.getX();
            float y = action.getY();
            
            for (com.aiassistant.data.models.DetectedEnemy enemy : currentState.getEnemies()) {
                if (enemy.contains(x, y)) {
                    // Direct hit
                    return 1.0f;
                }
                
                // Check if near enemy (within 10% of enemy size)
                float margin = Math.max(enemy.getWidth(), enemy.getHeight()) * 0.1f;
                if (x >= enemy.getX() - margin && x <= enemy.getX() + enemy.getWidth() + margin &&
                    y >= enemy.getY() - margin && y <= enemy.getY() + enemy.getHeight() + margin) {
                    // Near hit
                    return 0.5f;
                }
            }
            
            // Miss (small negative reward)
            return -0.1f;
        }
        
        return 0;
    }
    
    /**
     * Calculate resource management reward
     * 
     * @param previousState The previous state
     * @param currentState The current state
     * @return The reward
     */
    private float calculateResourceReward(GameState previousState, GameState currentState) {
        // For now, just focus on ammo
        int prevAmmo = previousState.getAmmoCount();
        int currAmmo = currentState.getAmmoCount();
        
        // Small penalty for using ammo, larger reward for gaining ammo
        if (currAmmo > prevAmmo) {
            return 0.5f; // Gained ammo
        } else if (currAmmo < prevAmmo) {
            return -0.1f; // Used ammo (small penalty)
        }
        
        return 0;
    }
    
    /**
     * Calculate objective progress reward
     * 
     * @param previousState The previous state
     * @param currentState The current state
     * @return The reward
     */
    private float calculateObjectiveReward(GameState previousState, GameState currentState) {
        // This would be game-specific and would need to detect objective progress
        // For now, return 0 as a placeholder
        return 0;
    }
    
    // Getters and setters for reward coefficients
    
    public float getHealthRewardCoef() {
        return healthRewardCoef;
    }
    
    public void setHealthRewardCoef(float healthRewardCoef) {
        this.healthRewardCoef = healthRewardCoef;
    }
    
    public float getEnemyDefeatRewardCoef() {
        return enemyDefeatRewardCoef;
    }
    
    public void setEnemyDefeatRewardCoef(float enemyDefeatRewardCoef) {
        this.enemyDefeatRewardCoef = enemyDefeatRewardCoef;
    }
    
    public float getSurvivalRewardCoef() {
        return survivalRewardCoef;
    }
    
    public void setSurvivalRewardCoef(float survivalRewardCoef) {
        this.survivalRewardCoef = survivalRewardCoef;
    }
    
    public float getAccuracyRewardCoef() {
        return accuracyRewardCoef;
    }
    
    public void setAccuracyRewardCoef(float accuracyRewardCoef) {
        this.accuracyRewardCoef = accuracyRewardCoef;
    }
    
    public float getResourceRewardCoef() {
        return resourceRewardCoef;
    }
    
    public void setResourceRewardCoef(float resourceRewardCoef) {
        this.resourceRewardCoef = resourceRewardCoef;
    }
    
    public float getObjectiveRewardCoef() {
        return objectiveRewardCoef;
    }
    
    public void setObjectiveRewardCoef(float objectiveRewardCoef) {
        this.objectiveRewardCoef = objectiveRewardCoef;
    }
    
    public float getExplicitFeedbackCoef() {
        return explicitFeedbackCoef;
    }
    
    public void setExplicitFeedbackCoef(float explicitFeedbackCoef) {
        this.explicitFeedbackCoef = explicitFeedbackCoef;
    }
}
