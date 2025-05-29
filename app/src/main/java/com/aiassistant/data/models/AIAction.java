package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AI action entity
 */
@Entity(tableName = "ai_actions")
@TypeConverters(Converters.class)
public class AIAction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private Date timestamp;
    private String actionType;
    private Map<String, String> parameters;
    private float confidence;
    private float expectedReward;
    private float actualReward;
    private String result;
    
    // Action types
    public static final String ACTION_TAP = "TAP";
    public static final String ACTION_SWIPE = "SWIPE";
    public static final String ACTION_LONG_PRESS = "LONG_PRESS";
    public static final String ACTION_TEXT_INPUT = "TEXT_INPUT";
    public static final String ACTION_WAIT = "WAIT";
    public static final String ACTION_BACK = "BACK";
    public static final String ACTION_HOME = "HOME";
    public static final String ACTION_VOICE_COMMAND = "VOICE_COMMAND";
    
    // Point class for touch coordinates
    public static class Point {
        public int x;
        public int y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Non-persisted fields for algorithm use
    private transient Point[] points;
    private transient GameState gameState;
    
    /**
     * Default constructor
     */
    public AIAction() {
        this.parameters = new HashMap<>();
        this.confidence = 0.0f;
        this.expectedReward = 0.0f;
        this.actualReward = 0.0f;
    }
    
    /**
     * Create a tap action
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return The action
     */
    public static AIAction createTapAction(int x, int y) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_TAP);
        action.addParameter("x", String.valueOf(x));
        action.addParameter("y", String.valueOf(y));
        action.setTimestamp(new Date());
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
     * @return The action
     */
    public static AIAction createSwipeAction(int startX, int startY, int endX, int endY, long duration) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_SWIPE);
        action.addParameter("startX", String.valueOf(startX));
        action.addParameter("startY", String.valueOf(startY));
        action.addParameter("endX", String.valueOf(endX));
        action.addParameter("endY", String.valueOf(endY));
        action.addParameter("duration", String.valueOf(duration));
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a long press action
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param duration Duration in milliseconds
     * @return The action
     */
    public static AIAction createLongPressAction(int x, int y, long duration) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_LONG_PRESS);
        action.addParameter("x", String.valueOf(x));
        action.addParameter("y", String.valueOf(y));
        action.addParameter("duration", String.valueOf(duration));
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a text input action
     * 
     * @param text The text
     * @return The action
     */
    public static AIAction createTextInputAction(String text) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_TEXT_INPUT);
        action.addParameter("text", text);
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a wait action
     * 
     * @param duration Duration in milliseconds
     * @return The action
     */
    public static AIAction createWaitAction(long duration) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_WAIT);
        action.addParameter("duration", String.valueOf(duration));
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a back action
     * 
     * @return The action
     */
    public static AIAction createBackAction() {
        AIAction action = new AIAction();
        action.setActionType(ACTION_BACK);
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a home action
     * 
     * @return The action
     */
    public static AIAction createHomeAction() {
        AIAction action = new AIAction();
        action.setActionType(ACTION_HOME);
        action.setTimestamp(new Date());
        return action;
    }
    
    /**
     * Create a voice command action
     * 
     * @param command The command
     * @return The action
     */
    public static AIAction createVoiceCommandAction(String command) {
        AIAction action = new AIAction();
        action.setActionType(ACTION_VOICE_COMMAND);
        action.addParameter("command", command);
        action.setTimestamp(new Date());
        return action;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }
    
    public String getParameter(String key) {
        return this.parameters.get(key);
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public float getExpectedReward() {
        return expectedReward;
    }
    
    public void setExpectedReward(float expectedReward) {
        this.expectedReward = expectedReward;
    }
    
    public float getActualReward() {
        return actualReward;
    }
    
    public void setActualReward(float actualReward) {
        this.actualReward = actualReward;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * Get points
     * @return Points
     */
    public Point[] getPoints() {
        if (points == null && ACTION_TAP.equals(actionType)) {
            // Extract points from parameters
            try {
                int x = Integer.parseInt(getParameter("x"));
                int y = Integer.parseInt(getParameter("y"));
                points = new Point[] { new Point(x, y) };
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return points;
    }
    
    /**
     * Set points
     * @param points Points
     */
    public void setPoints(Point[] points) {
        this.points = points;
        
        // Update parameters based on points
        if (points != null && points.length > 0) {
            addParameter("x", String.valueOf(points[0].x));
            addParameter("y", String.valueOf(points[0].y));
        }
    }
    
    /**
     * Get game state
     * @return Game state
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Set game state
     * @param gameState Game state
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
