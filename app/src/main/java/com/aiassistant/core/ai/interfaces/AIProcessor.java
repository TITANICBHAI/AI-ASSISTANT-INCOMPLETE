package com.aiassistant.core.ai.interfaces;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.ScreenActionEntity;

/**
 * Interface for AI processors that can analyze game states and generate actions
 */
public interface AIProcessor {
    
    /**
     * Process a game state and generate an action
     * 
     * @param gameState The current game state
     * @return The generated action, or null if no action should be taken
     */
    AIAction processGameState(GameState gameState);
    
    /**
     * Process user action for learning
     * 
     * @param gameState The game state when the action was taken
     * @param action The user action
     * @param reward The reward for the action
     */
    void processUserAction(GameState gameState, ScreenActionEntity action, float reward);
    
    /**
     * Set the mode for the AI processor
     * 
     * @param mode The mode to set
     */
    void setMode(int mode);
    
    /**
     * Get the current mode
     * 
     * @return The current mode
     */
    int getMode();
    
    /**
     * Reset the AI processor state
     * 
     * @param gameId The game ID to reset
     */
    void reset(String gameId);
    
    /**
     * Save the AI processor state
     * 
     * @return True if save successful
     */
    boolean save();
}
