package com.aiassistant.core.ai.execution;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.ScreenActionEntity;

/**
 * Interface for executing screen actions
 */
public interface ActionExecutor {
    
    /**
     * Execute a tap at the specified coordinates
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return True if executed successfully
     */
    boolean executeTap(float x, float y);
    
    /**
     * Execute a swipe from the start coordinates to the end coordinates
     * 
     * @param startX The start x coordinate
     * @param startY The start y coordinate
     * @param endX The end x coordinate
     * @param endY The end y coordinate
     * @param duration The duration in milliseconds
     * @return True if executed successfully
     */
    boolean executeSwipe(float startX, float startY, float endX, float endY, long duration);
    
    /**
     * Execute a long press at the specified coordinates
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param duration The duration in milliseconds
     * @return True if executed successfully
     */
    boolean executeLongPress(float x, float y, long duration);
    
    /**
     * Execute a text input at the specified UI element
     * 
     * @param elementId The UI element ID
     * @param text The text to input
     * @return True if executed successfully
     */
    boolean executeTextInput(long elementId, String text);
    
    /**
     * Execute a back button press
     * 
     * @return True if executed successfully
     */
    boolean executeBack();
    
    /**
     * Execute an AI action
     * 
     * @param action The AI action to execute
     * @return True if executed successfully
     */
    boolean executeAIAction(AIAction action);
    
    /**
     * Execute a screen action
     * 
     * @param action The screen action to execute
     * @return True if executed successfully
     */
    boolean executeScreenAction(ScreenActionEntity action);
}
