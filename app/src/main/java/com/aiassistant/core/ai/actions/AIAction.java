package com.aiassistant.core.ai.actions;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action that the AI can take
 */
public class AIAction {
    private static final String TAG = "AIAction";
    
    /**
     * Types of actions the AI can perform
     */
    public enum ActionType {
        TAP,
        SWIPE,
        MULTI_TAP,
        HOLD,
        COMPLEX_GESTURE,
        WAIT,
        CANCEL
    }
    
    private ActionType type;
    private List<PointF> points;
    private long duration;
    private float confidence;
    private String targetElementId;
    private boolean isExecuted;
    
    /**
     * Constructor for tap action
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public AIAction(float x, float y) {
        this.type = ActionType.TAP;
        this.points = new ArrayList<>();
        this.points.add(new PointF(x, y));
        this.duration = 100; // Default tap duration in ms
        this.confidence = 1.0f;
        this.isExecuted = false;
    }
    
    /**
     * Constructor for specific action type
     * 
     * @param type Action type
     * @param points List of points (coordinates)
     * @param duration Duration in milliseconds
     */
    public AIAction(ActionType type, List<PointF> points, long duration) {
        this.type = type;
        this.points = new ArrayList<>(points);
        this.duration = duration;
        this.confidence = 1.0f;
        this.isExecuted = false;
    }
    
    /**
     * Create a tap action
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return The created action
     */
    public static AIAction createTap(float x, float y) {
        AIAction action = new AIAction(ActionType.TAP, new ArrayList<>(), 100);
        action.points.add(new PointF(x, y));
        return action;
    }
    
    /**
     * Create a swipe action
     * 
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param duration Duration in milliseconds
     * @return The created action
     */
    public static AIAction createSwipe(float startX, float startY, float endX, float endY, long duration) {
        List<PointF> points = new ArrayList<>();
        points.add(new PointF(startX, startY));
        points.add(new PointF(endX, endY));
        return new AIAction(ActionType.SWIPE, points, duration);
    }
    
    /**
     * Create a hold action
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param duration Duration in milliseconds
     * @return The created action
     */
    public static AIAction createHold(float x, float y, long duration) {
        List<PointF> points = new ArrayList<>();
        points.add(new PointF(x, y));
        return new AIAction(ActionType.HOLD, points, duration);
    }
    
    /**
     * Create a wait action
     * 
     * @param duration Duration to wait in milliseconds
     * @return The created action
     */
    public static AIAction createWait(long duration) {
        return new AIAction(ActionType.WAIT, new ArrayList<>(), duration);
    }
    
    /**
     * Create a cancel action
     * 
     * @return The created action
     */
    public static AIAction createCancel() {
        return new AIAction(ActionType.CANCEL, new ArrayList<>(), 0);
    }
    
    /**
     * Get the action type
     * 
     * @return The action type
     */
    public ActionType getType() {
        return type;
    }
    
    /**
     * Set the action type
     * 
     * @param type The action type
     */
    public void setType(ActionType type) {
        this.type = type;
    }
    
    /**
     * Get the points (coordinates)
     * 
     * @return The points
     */
    public List<PointF> getPoints() {
        return points;
    }
    
    /**
     * Set the points (coordinates)
     * 
     * @param points The points
     */
    public void setPoints(List<PointF> points) {
        this.points = points;
    }
    
    /**
     * Get the duration
     * 
     * @return The duration in milliseconds
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * Set the duration
     * 
     * @param duration The duration in milliseconds
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    /**
     * Get the confidence
     * 
     * @return The confidence (0-1)
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set the confidence
     * 
     * @param confidence The confidence (0-1)
     */
    public void setConfidence(float confidence) {
        this.confidence = Math.max(0, Math.min(1, confidence));
    }
    
    /**
     * Get the target element ID
     * 
     * @return The target element ID
     */
    public String getTargetElementId() {
        return targetElementId;
    }
    
    /**
     * Set the target element ID
     * 
     * @param targetElementId The target element ID
     */
    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }
    
    /**
     * Check if the action is executed
     * 
     * @return Whether the action is executed
     */
    public boolean isExecuted() {
        return isExecuted;
    }
    
    /**
     * Set whether the action is executed
     * 
     * @param executed Whether the action is executed
     */
    public void setExecuted(boolean executed) {
        isExecuted = executed;
    }
    
    /**
     * Get a description of the action
     * 
     * @return The description
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        
        if (points.size() > 0) {
            sb.append(" at ");
            for (int i = 0; i < points.size(); i++) {
                PointF point = points.get(i);
                sb.append(String.format("(%.1f, %.1f)", point.x, point.y));
                if (i < points.size() - 1) {
                    sb.append(" to ");
                }
            }
        }
        
        if (duration > 0 && type != ActionType.TAP) {
            sb.append(String.format(" for %d ms", duration));
        }
        
        if (targetElementId != null && !targetElementId.isEmpty()) {
            sb.append(" on element ").append(targetElementId);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}
