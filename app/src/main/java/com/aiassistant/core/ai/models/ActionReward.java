package com.aiassistant.core.ai.models;

import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;

/**
 * Action reward model for reinforcement learning
 */
public class ActionReward {
    private static final String TAG = "ActionReward";
    
    // Reward weights
    private static final float WEIGHT_MOVEMENT = 0.3f;
    private static final float WEIGHT_COMBAT = 0.4f;
    private static final float WEIGHT_EXPLORATION = 0.2f;
    private static final float WEIGHT_OBJECTIVE = 0.5f;
    private static final float WEIGHT_SURVIVAL = 0.6f;
    private static final float WEIGHT_RESOURCE = 0.3f;
    private static final float WEIGHT_PENALTY = -0.5f;
    
    /**
     * Calculate reward for an action
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The calculated reward value
     */
    public static float calculateReward(AIAction action, GameState previousState, GameState currentState) {
        if (action == null || previousState == null || currentState == null) {
            return 0.0f;
        }
        
        try {
            float reward = 0.0f;
            
            // Base reward components
            reward += calculateExplorationReward(action, previousState, currentState);
            reward += calculateMovementReward(action, previousState, currentState);
            reward += calculateSurvivalReward(action, previousState, currentState);
            reward += calculateInteractionReward(action, previousState, currentState);
            
            // Penalties
            reward += calculatePenalties(action, previousState, currentState);
            
            // Combat and objective rewards
            reward += calculateCombatReward(action, previousState, currentState);
            reward += calculateObjectiveReward(action, previousState, currentState);
            
            return reward;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating reward: " + e.getMessage());
            return 0.0f;
        }
    }
    
    /**
     * Calculate exploration reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The exploration reward component
     */
    private static float calculateExplorationReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        boolean isNewArea = isNewAreaDetected(previousState, currentState);
        
        if (isNewArea) {
            reward += 1.0f * WEIGHT_EXPLORATION;
        }
        
        return reward;
    }
    
    /**
     * Detect if the current state represents a new area
     * 
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return true if new area detected
     */
    private static boolean isNewAreaDetected(GameState previousState, GameState currentState) {
        if (previousState == null || currentState == null) {
            return false;
        }
        
        String prevScreenHash = generateScreenHash(previousState);
        String currScreenHash = generateScreenHash(currentState);
        
        if (prevScreenHash == null || currScreenHash == null) {
            return false;
        }
        
        int hashDifference = hammingDistance(prevScreenHash, currScreenHash);
        
        float similarityThreshold = 0.3f;
        float similarity = 1.0f - (hashDifference / (float) Math.max(prevScreenHash.length(), 1));
        
        return similarity < (1.0f - similarityThreshold);
    }
    
    /**
     * Generate a simple hash of the screen state
     */
    private static String generateScreenHash(GameState state) {
        if (state == null) {
            return null;
        }
        
        StringBuilder hash = new StringBuilder();
        
        String screenText = state.getScreenText();
        if (screenText != null && !screenText.isEmpty()) {
            hash.append(screenText.hashCode());
        } else {
            hash.append("0");
        }
        
        hash.append("-");
        hash.append(state.getEnemies() != null ? state.getEnemies().size() : 0);
        hash.append("-");
        hash.append((int)state.getPlayerHealth());
        hash.append("-");
        hash.append((int)state.getPlayerAmmo());
        
        return hash.toString();
    }
    
    /**
     * Calculate Hamming distance between two strings
     */
    private static int hammingDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }
        
        int distance = Math.abs(s1.length() - s2.length());
        int minLength = Math.min(s1.length(), s2.length());
        
        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                distance++;
            }
        }
        
        return distance;
    }
    
    /**
     * Calculate movement reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The movement reward component
     */
    private static float calculateMovementReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        // Simple distance calculation - reward for significant movement
        float movementDistance = calculateMovementDistance(previousState, currentState);
        
        if (movementDistance > 0) {
            // Normalize the distance to a reasonable range
            float normalizedDistance = Math.min(1.0f, movementDistance / 100.0f);
            reward += normalizedDistance * WEIGHT_MOVEMENT;
        }
        
        return reward;
    }
    
    /**
     * Calculate movement distance between states
     * 
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The movement distance
     */
    private static float calculateMovementDistance(GameState previousState, GameState currentState) {
        // In a real implementation, this would calculate actual position change
        // For now, just return a basic estimate based on screen changes
        
        // Simple placeholder implementation
        if (previousState.getScreenText() != null && currentState.getScreenText() != null) {
            if (!previousState.getScreenText().equals(currentState.getScreenText())) {
                return 50.0f;  // Significant change detected
            }
        }
        
        return 0.0f;
    }
    
    /**
     * Calculate survival reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The survival reward component
     */
    private static float calculateSurvivalReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        // Reward for surviving and maintaining/improving health
        
        // Basic placeholder implementation - would be replaced with real health tracking
        // For demonstration purposes only
        boolean playerHealthy = true;  // Placeholder, would check actual health
        
        if (playerHealthy) {
            reward += 0.1f * WEIGHT_SURVIVAL;  // Small reward for staying alive
        }
        
        return reward;
    }
    
    /**
     * Calculate interaction reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The interaction reward component
     */
    private static float calculateInteractionReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        // Reward for successful interactions with UI elements
        if (AIAction.ACTION_TAP.equals(action.getActionType())) {
            // Check if tap resulted in UI change
            if (previousState.getScreenText() != null && currentState.getScreenText() != null) {
                if (!previousState.getScreenText().equals(currentState.getScreenText())) {
                    reward += 0.2f;  // Reward for successful interaction
                }
            }
        } else if (AIAction.ACTION_SWIPE.equals(action.getActionType())) {
            // Check if swipe resulted in significant change
            // Simple placeholder implementation
            if (previousState.getScreenText() != null && currentState.getScreenText() != null) {
                if (!previousState.getScreenText().equals(currentState.getScreenText())) {
                    reward += 0.15f;  // Reward for successful swipe
                }
            }
        }
        
        return reward;
    }
    
    /**
     * Calculate penalty components
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The penalty component
     */
    private static float calculatePenalties(AIAction action, GameState previousState, GameState currentState) {
        float penalty = 0.0f;
        
        // Penalties for ineffective actions
        if (previousState.getScreenText() != null && currentState.getScreenText() != null) {
            if (previousState.getScreenText().equals(currentState.getScreenText())) {
                // No change in screen content - action may have been ineffective
                penalty += -0.05f * WEIGHT_PENALTY;
            }
        }
        
        // Penalty for repeated actions (would need action history)
        
        return penalty;
    }
    
    /**
     * Calculate combat reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The combat reward component
     */
    private static float calculateCombatReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        if (previousState == null || currentState == null) {
            return reward;
        }
        
        int prevEnemyCount = previousState.getEnemies() != null ? previousState.getEnemies().size() : 0;
        int currEnemyCount = currentState.getEnemies() != null ? currentState.getEnemies().size() : 0;
        
        int enemiesEliminated = prevEnemyCount - currEnemyCount;
        
        if (enemiesEliminated > 0) {
            reward += enemiesEliminated * 2.0f * WEIGHT_COMBAT;
            Log.d(TAG, "Combat reward: eliminated " + enemiesEliminated + " enemies");
        }
        
        float prevHealth = previousState.getPlayerHealth();
        float currHealth = currentState.getPlayerHealth();
        float healthChange = currHealth - prevHealth;
        
        if (healthChange > 0) {
            reward += healthChange * 0.01f * WEIGHT_COMBAT;
        } else if (healthChange < 0) {
            reward += healthChange * 0.02f * WEIGHT_COMBAT;
        }
        
        boolean wasInCombat = previousState.isInCombat();
        boolean isInCombat = currentState.isInCombat();
        
        if (wasInCombat && !isInCombat && currHealth > prevHealth * 0.5f) {
            reward += 1.0f * WEIGHT_COMBAT;
            Log.d(TAG, "Combat reward: survived combat encounter");
        }
        
        return reward;
    }
    
    /**
     * Calculate objective reward component
     * 
     * @param action The action taken
     * @param previousState The previous game state
     * @param currentState The current game state
     * @return The objective reward component
     */
    private static float calculateObjectiveReward(AIAction action, GameState previousState, GameState currentState) {
        float reward = 0.0f;
        
        if (previousState == null || currentState == null) {
            return reward;
        }
        
        boolean objectiveProgress = false;
        
        String prevText = previousState.getScreenText();
        String currText = currentState.getScreenText();
        
        if (prevText != null && currText != null) {
            String[] objectiveKeywords = {"complete", "mission", "objective", "win", "victory", "success", "cleared"};
            
            for (String keyword : objectiveKeywords) {
                if (!prevText.toLowerCase().contains(keyword) && currText.toLowerCase().contains(keyword)) {
                    objectiveProgress = true;
                    break;
                }
            }
        }
        
        if (objectiveProgress) {
            reward += 3.0f * WEIGHT_OBJECTIVE;
            Log.d(TAG, "Objective reward: objective progress detected");
        }
        
        float prevAmmo = previousState.getPlayerAmmo();
        float currAmmo = currentState.getPlayerAmmo();
        
        if (currAmmo > prevAmmo) {
            reward += 0.2f * WEIGHT_RESOURCE;
            Log.d(TAG, "Resource reward: collected ammo/resources");
        }
        
        return reward;
    }
}
