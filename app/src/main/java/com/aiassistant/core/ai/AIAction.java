package com.aiassistant.core.ai;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action that the AI can take
 * This is used to represent UI interactions, gestures, and game actions
 */
public class AIAction {
    // Action types
    public static final String ACTION_TAP = "tap";
    public static final String ACTION_LONG_PRESS = "long_press";
    public static final String ACTION_SWIPE = "swipe";
    public static final String ACTION_MULTI_TOUCH = "multi_touch";
    public static final String ACTION_TAP_ELEMENT = "tap_element";
    public static final String ACTION_COMBO = "combo";
    public static final String ACTION_COMBAT_MANEUVER = "combat_maneuver";
    public static final String ACTION_WAIT = "wait";
    
    // Action type
    private String actionType;
    
    // Action parameters
    private List<Point> points = new ArrayList<>();
    private int duration = 0;
    private float confidence = 0.0f;
    
    // Target element info (for element-based actions)
    private String targetElementId;
    
    // Special action parameters
    private String comboName;
    private String combatManeuver;
    
    // Action description
    private String description;
    
    /**
     * Default constructor
     */
    public AIAction() {
    }
    
    /**
     * Constructor with action type
     */
    public AIAction(String actionType) {
        this.actionType = actionType;
    }
    
    /**
     * Constructor for tap action
     */
    public static AIAction createTapAction(int x, int y) {
        AIAction action = new AIAction(ACTION_TAP);
        action.addPoint(new Point(x, y));
        return action;
    }
    
    /**
     * Constructor for long press action
     */
    public static AIAction createLongPressAction(int x, int y, int duration) {
        AIAction action = new AIAction(ACTION_LONG_PRESS);
        action.addPoint(new Point(x, y));
        action.setDuration(duration);
        return action;
    }
    
    /**
     * Constructor for swipe action
     */
    public static AIAction createSwipeAction(int startX, int startY, int endX, int endY, int duration) {
        AIAction action = new AIAction(ACTION_SWIPE);
        action.addPoint(new Point(startX, startY));
        action.addPoint(new Point(endX, endY));
        action.setDuration(duration);
        return action;
    }
    
    /**
     * Constructor for multi-touch action
     */
    public static AIAction createMultiTouchAction(
            int finger1StartX, int finger1StartY, int finger1EndX, int finger1EndY,
            int finger2StartX, int finger2StartY, int finger2EndX, int finger2EndY,
            int duration) {
        AIAction action = new AIAction(ACTION_MULTI_TOUCH);
        action.addPoint(new Point(finger1StartX, finger1StartY));
        action.addPoint(new Point(finger1EndX, finger1EndY));
        action.addPoint(new Point(finger2StartX, finger2StartY));
        action.addPoint(new Point(finger2EndX, finger2EndY));
        action.setDuration(duration);
        return action;
    }
    
    /**
     * Constructor for element tap action
     */
    public static AIAction createElementTapAction(String elementId) {
        AIAction action = new AIAction(ACTION_TAP_ELEMENT);
        action.setTargetElementId(elementId);
        return action;
    }
    
    /**
     * Constructor for combo action
     */
    public static AIAction createComboAction(String comboName) {
        AIAction action = new AIAction(ACTION_COMBO);
        action.setComboName(comboName);
        return action;
    }
    
    /**
     * Constructor for combat maneuver action
     */
    public static AIAction createCombatManeuverAction(String maneuverType, int targetX, int targetY) {
        AIAction action = new AIAction(ACTION_COMBAT_MANEUVER);
        action.setCombatManeuver(maneuverType);
        action.addPoint(new Point(targetX, targetY));
        return action;
    }
    
    /**
     * Constructor for wait action
     */
    public static AIAction createWaitAction(int duration) {
        AIAction action = new AIAction(ACTION_WAIT);
        action.setDuration(duration);
        return action;
    }
    
    // Getters and setters
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public List<Point> getPoints() {
        return points;
    }
    
    public void setPoints(List<Point> points) {
        this.points = points;
    }
    
    public void addPoint(Point point) {
        if (points == null) {
            points = new ArrayList<>();
        }
        points.add(point);
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public String getTargetElementId() {
        return targetElementId;
    }
    
    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }
    
    public String getComboName() {
        return comboName;
    }
    
    public void setComboName(String comboName) {
        this.comboName = comboName;
    }
    
    public String getCombatManeuver() {
        return combatManeuver;
    }
    
    public void setCombatManeuver(String combatManeuver) {
        this.combatManeuver = combatManeuver;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AIAction{")
                .append("type='").append(actionType).append('\'')
                .append(", confidence=").append(confidence);
        
        if (points != null && !points.isEmpty()) {
            sb.append(", points=[");
            for (Point p : points) {
                sb.append("(").append(p.x).append(",").append(p.y).append("),");
            }
            sb.append("]");
        }
        
        if (duration > 0) {
            sb.append(", duration=").append(duration);
        }
        
        if (targetElementId != null) {
            sb.append(", target='").append(targetElementId).append('\'');
        }
        
        if (comboName != null) {
            sb.append(", combo='").append(comboName).append('\'');
        }
        
        if (combatManeuver != null) {
            sb.append(", maneuver='").append(combatManeuver).append('\'');
        }
        
        sb.append('}');
        return sb.toString();
    }
}
