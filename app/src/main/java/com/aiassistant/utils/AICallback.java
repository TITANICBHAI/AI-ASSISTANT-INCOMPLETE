package com.aiassistant.utils;

import android.graphics.Bitmap;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;

/**
 * Callback interface for AI-related events
 */
public interface AICallback {
    
    /**
     * Called when AI has processed a screenshot
     * 
     * @param gameState The game state
     * @param enemies The detected enemies
     * @param uiElements The detected UI elements
     * @param inCombat Whether the player is in combat
     */
    void onScreenProcessed(GameState gameState);
    
    /**
     * Called when AI has executed an action
     * 
     * @param action The action
     * @param success Whether the action was successful
     */
    void onActionExecuted(AIAction action, boolean success);
    
    /**
     * Called when AI suggests an action
     * 
     * @param action The action
     * @param confidence The confidence level
     */
    void onActionSuggested(AIAction action, float confidence);
    
    /**
     * Called when AI state changes
     * 
     * @param newState The new state
     */
    void onAIStateChanged(AIStateManager.AIState newState);
    
    /**
     * Called when AI mode changes
     * 
     * @param mode The new mode
     */
    void onAIModeChanged(AIStateManager.AIState mode);
    
    /**
     * Called when an error occurs
     * 
     * @param message The error message
     * @param e The exception
     */
    void onError(String message, Exception e);
}
