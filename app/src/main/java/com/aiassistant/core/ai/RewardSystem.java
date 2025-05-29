package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.actions.AIAction;
import com.aiassistant.data.models.DetectedEnemy;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.UIElement;

import java.util.HashMap;
import java.util.Map;

/**
 * System for calculating rewards for reinforcement learning
 */
public class RewardSystem {
    
    private static final String TAG = "RewardSystem";
    
    // Context
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context The context
     */
    public RewardSystem(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Calculate reward for an action
     * 
     * @param state The state
     * @param action The action
     * @param nextState The next state
     * @return The reward
     */
    public float calculateReward(GameState state, AIAction action, GameState nextState) {
        if (state == null || action == null || nextState == null) {
            return 0;
        }
        
        try {
            float reward = 0;
            
            // Reward for health changes
            float healthChange = nextState.getPlayerHealth() - state.getPlayerHealth();
            if (healthChange > 0) {
                // Health gained
                reward += 0.5f;
            } else if (healthChange < 0) {
                // Health lost
                reward -= 0.3f;
            }
            
            // Reward for enemy status changes
            int enemiesVisible = countVisibleEnemies(state);
            int nextEnemiesVisible = countVisibleEnemies(nextState);
            int enemiesEliminated = countEliminatedEnemies(state, nextState);
            
            if (enemiesEliminated > 0) {
                // Enemies eliminated
                reward += 1.0f * enemiesEliminated;
            }
            
            if (nextEnemiesVisible < enemiesVisible) {
                // Fewer enemies visible (might have hidden)
                reward += 0.2f;
            }
            
            // Reward for UI interactions
            if (action.getType() == AIAction.ActionType.TAP) {
                UIElement targetElement = findElementAtAction(state, action);
                if (targetElement != null && targetElement.isClickable()) {
                    // Clicked on a UI element
                    reward += 0.1f;
                }
            }
            
            // Penalize waiting too long
            if (action.getType() == AIAction.ActionType.WAIT) {
                reward -= 0.1f;
            }
            
            // Clip reward to reasonable range
            reward = Math.max(-1.0f, Math.min(1.0f, reward));
            
            return reward;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating reward: " + e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Count visible enemies
     * 
     * @param state The state
     * @return The number of visible enemies
     */
    private int countVisibleEnemies(GameState state) {
        int count = 0;
        for (DetectedEnemy enemy : state.getEnemies()) {
            if (enemy.isVisible()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Count eliminated enemies
     * 
     * @param state The state
     * @param nextState The next state
     * @return The number of eliminated enemies
     */
    private int countEliminatedEnemies(GameState state, GameState nextState) {
        int count = 0;
        
        // Create map of enemy IDs to enemies
        Map<String, DetectedEnemy> enemyMap = new HashMap<>();
        for (DetectedEnemy enemy : state.getEnemies()) {
            if (enemy.getId() != null) {
                enemyMap.put(enemy.getId(), enemy);
            }
        }
        
        // Check which enemies are no longer present or have zero health
        for (Map.Entry<String, DetectedEnemy> entry : enemyMap.entrySet()) {
            String enemyId = entry.getKey();
            DetectedEnemy enemy = entry.getValue();
            
            boolean foundInNext = false;
            for (DetectedEnemy nextEnemy : nextState.getEnemies()) {
                if (enemyId.equals(nextEnemy.getId())) {
                    foundInNext = true;
                    if (enemy.getHealth() > 0 && nextEnemy.getHealth() <= 0) {
                        // Enemy was eliminated
                        count++;
                    }
                    break;
                }
            }
            
            if (!foundInNext && enemy.isVisible()) {
                // Enemy was visible but is no longer present
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Find UI element at action
     * 
     * @param state The state
     * @param action The action
     * @return The UI element or null
     */
    private UIElement findElementAtAction(GameState state, AIAction action) {
        if (action.getPoints().isEmpty()) {
            return null;
        }
        
        float x = action.getPoints().get(0).x;
        float y = action.getPoints().get(0).y;
        
        for (UIElement element : state.getUiElements()) {
            if (element.containsPoint(x, y)) {
                return element;
            }
        }
        
        return null;
    }
}
