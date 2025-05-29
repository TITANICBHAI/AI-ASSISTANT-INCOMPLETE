package com.aiassistant.core.ai.models;

import com.aiassistant.data.models.UIElement;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an action that can be performed in a game.
 * This includes touch actions, key presses, swipes, and more complex gestures.
 */
public class GameAction {

    // Action types
    public static final String TYPE_TOUCH = "touch";
    public static final String TYPE_LONG_PRESS = "long_press";
    public static final String TYPE_SWIPE = "swipe";
    public static final String TYPE_KEY = "key";
    public static final String TYPE_MULTI_TOUCH = "multi_touch";
    public static final String TYPE_BACK = "back";
    public static final String TYPE_HOME = "home";
    
    // Core properties
    private final String id;
    private final String type;
    private final long timestamp;
    
    // Touch coordinates
    private int touchX = -1;
    private int touchY = -1;
    
    // For swipes, the end coordinates
    private int touchEndX = -1;
    private int touchEndY = -1;
    
    // For key actions
    private int keyCode = -1;
    
    // Target element (if known)
    private UIElement targetElement;
    
    // Action duration (for long press or swipes)
    private long durationMs = 0;
    
    // For multi-touch actions
    private int[] multiTouchX;
    private int[] multiTouchY;
    
    /**
     * Create a new game action
     * @param type Action type
     */
    public GameAction(String type) {
        this.id = generateId();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a new touch action
     * @param x X coordinate
     * @param y Y coordinate
     * @return GameAction instance
     */
    public static GameAction createTouch(int x, int y) {
        GameAction action = new GameAction(TYPE_TOUCH);
        action.touchX = x;
        action.touchY = y;
        return action;
    }
    
    /**
     * Create a new long press action
     * @param x X coordinate
     * @param y Y coordinate
     * @param durationMs Duration in milliseconds
     * @return GameAction instance
     */
    public static GameAction createLongPress(int x, int y, long durationMs) {
        GameAction action = new GameAction(TYPE_LONG_PRESS);
        action.touchX = x;
        action.touchY = y;
        action.durationMs = durationMs;
        return action;
    }
    
    /**
     * Create a new swipe action
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param durationMs Duration in milliseconds
     * @return GameAction instance
     */
    public static GameAction createSwipe(int startX, int startY, int endX, int endY, long durationMs) {
        GameAction action = new GameAction(TYPE_SWIPE);
        action.touchX = startX;
        action.touchY = startY;
        action.touchEndX = endX;
        action.touchEndY = endY;
        action.durationMs = durationMs;
        return action;
    }
    
    /**
     * Create a new key action
     * @param keyCode Android key code
     * @return GameAction instance
     */
    public static GameAction createKey(int keyCode) {
        GameAction action = new GameAction(TYPE_KEY);
        action.keyCode = keyCode;
        return action;
    }
    
    /**
     * Create a new back action
     * @return GameAction instance
     */
    public static GameAction createBack() {
        return new GameAction(TYPE_BACK);
    }
    
    /**
     * Create a new home action
     * @return GameAction instance
     */
    public static GameAction createHome() {
        return new GameAction(TYPE_HOME);
    }
    
    /**
     * Create a new multi-touch action
     * @param x Array of X coordinates
     * @param y Array of Y coordinates
     * @return GameAction instance
     */
    public static GameAction createMultiTouch(int[] x, int[] y) {
        if (x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("X and Y arrays must be the same length and not empty");
        }
        
        GameAction action = new GameAction(TYPE_MULTI_TOUCH);
        action.multiTouchX = x.clone();
        action.multiTouchY = y.clone();
        return action;
    }
    
    /**
     * Generate a unique ID for this action
     * @return Unique ID
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getTouchX() {
        return touchX;
    }
    
    public int getTouchY() {
        return touchY;
    }
    
    public int getTouchEndX() {
        return touchEndX;
    }
    
    public int getTouchEndY() {
        return touchEndY;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
    
    public UIElement getTargetElement() {
        return targetElement;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public int[] getMultiTouchX() {
        return multiTouchX != null ? multiTouchX.clone() : null;
    }
    
    public int[] getMultiTouchY() {
        return multiTouchY != null ? multiTouchY.clone() : null;
    }
    
    // Setters
    
    public void setTouchX(int touchX) {
        this.touchX = touchX;
    }
    
    public void setTouchY(int touchY) {
        this.touchY = touchY;
    }
    
    public void setTouchEndX(int touchEndX) {
        this.touchEndX = touchEndX;
    }
    
    public void setTouchEndY(int touchEndY) {
        this.touchEndY = touchEndY;
    }
    
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
    
    public void setTargetElement(UIElement targetElement) {
        this.targetElement = targetElement;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public void setMultiTouchX(int[] multiTouchX) {
        this.multiTouchX = multiTouchX != null ? multiTouchX.clone() : null;
    }
    
    public void setMultiTouchY(int[] multiTouchY) {
        this.multiTouchY = multiTouchY != null ? multiTouchY.clone() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameAction that = (GameAction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        StringBuilder desc = new StringBuilder(type.toUpperCase());
        
        switch (type) {
            case TYPE_TOUCH:
                desc.append(" at (").append(touchX).append(", ").append(touchY).append(")");
                break;
                
            case TYPE_LONG_PRESS:
                desc.append(" at (").append(touchX).append(", ").append(touchY).append(") for ").append(durationMs).append("ms");
                break;
                
            case TYPE_SWIPE:
                desc.append(" from (").append(touchX).append(", ").append(touchY).append(") to (")
                    .append(touchEndX).append(", ").append(touchEndY).append(") for ").append(durationMs).append("ms");
                break;
                
            case TYPE_KEY:
                desc.append(" keyCode=").append(keyCode);
                break;
                
            case TYPE_MULTI_TOUCH:
                desc.append(" with ").append(multiTouchX != null ? multiTouchX.length : 0).append(" points");
                break;
        }
        
        if (targetElement != null) {
            desc.append(" targeting element ID ").append(targetElement.getId());
        }
        
        return desc.toString();
    }
}
