package com.aiassistant.core.ai.interfaces;

/**
 * Interface for receiving AI state change notifications
 */
public interface AIStateListener {
    /**
     * Called when the AI state changes
     * 
     * @param aiMode The current AI mode
     * @param isRunning Whether the AI is running
     */
    void onAIStateChanged(int aiMode, boolean isRunning);
    
    /**
     * Called when actions are executed by the AI
     * 
     * @param actionValues The action values that were executed
     */
    void onActionsExecuted(float[] actionValues);
    
    /**
     * Called when actions are suggested by the AI
     * 
     * @param actionValues The action values that were suggested
     */
    void onActionsSuggested(float[] actionValues);
}
