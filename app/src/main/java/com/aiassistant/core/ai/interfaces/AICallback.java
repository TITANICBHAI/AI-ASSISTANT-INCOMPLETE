package com.aiassistant.core.ai.interfaces;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.UIElement;

import java.util.List;

/**
 * Callback interface for AI-related events and operations
 */
public interface AICallback {
    /**
     * Called when the AI state changes
     * 
     * @param newState The new state
     * @param oldState The previous state
     */
    void onAIStateChanged(int newState, int oldState);
    
    /**
     * Called when the AI suggests an action
     * 
     * @param action The suggested action
     * @param confidence The confidence level (0.0 - 1.0)
     */
    void onActionSuggested(AIAction action, float confidence);
    
    /**
     * Called when the AI performs an action
     * 
     * @param action The action being performed
     */
    void onActionPerformed(AIAction action);
    
    /**
     * Called when the AI action is completed
     * 
     * @param action The completed action
     * @param successful Whether the action was successful
     */
    void onActionCompleted(AIAction action, boolean successful);
    
    /**
     * Called when the game state is updated
     * 
     * @param state The current game state
     * @param detectedElements The UI elements detected in the current state
     */
    void onGameStateUpdated(GameState state, List<UIElement> detectedElements);
    
    /**
     * Called when an error occurs during AI operation
     * 
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    void onAIError(int errorCode, String errorMessage);
    
    /**
     * Called to log a debug message
     * 
     * @param tag The log tag
     * @param message The log message
     */
    void onDebugLog(String tag, String message);
}
