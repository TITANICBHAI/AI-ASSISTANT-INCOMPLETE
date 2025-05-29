package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Model class for action suggestions
 */
@Entity(tableName = "action_suggestions",
        indices = {@Index("gameStateId"), @Index("gameId")})
public class ActionSuggestion {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long gameStateId;
    private String gameId;
    private int actionType;
    private float x;
    private float y;
    private float endX;
    private float endY;
    private long duration;
    private String text;
    private float confidence;
    private boolean isAccepted;
    private long timestamp;
    
    /**
     * Default constructor
     */
    public ActionSuggestion() {
        this.timestamp = System.currentTimeMillis();
        this.isAccepted = false;
    }
    
    /**
     * Constructor
     * 
     * @param gameStateId The game state ID
     * @param gameId The game ID
     * @param actionType The action type
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param confidence The confidence level
     */
    public ActionSuggestion(long gameStateId, String gameId, int actionType, float x, float y, float confidence) {
        this();
        this.gameStateId = gameStateId;
        this.gameId = gameId;
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.confidence = confidence;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getGameStateId() {
        return gameStateId;
    }
    
    public void setGameStateId(long gameStateId) {
        this.gameStateId = gameStateId;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public int getActionType() {
        return actionType;
    }
    
    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getEndX() {
        return endX;
    }
    
    public void setEndX(float endX) {
        this.endX = endX;
    }
    
    public float getEndY() {
        return endY;
    }
    
    public void setEndY(float endY) {
        this.endY = endY;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }
    
    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Convert to a screen action
     * 
     * @return The screen action
     */
    public ScreenActionEntity toScreenAction() {
        ScreenActionEntity action = new ScreenActionEntity();
        action.setActionType(actionType);
        action.setX(x);
        action.setY(y);
        action.setEndX(endX);
        action.setEndY(endY);
        action.setDuration(duration);
        action.setText(text);
        return action;
    }
    
    /**
     * Create from an AI action
     * 
     * @param gameStateId The game state ID
     * @param gameId The game ID
     * @param aiAction The AI action
     * @return The action suggestion
     */
    public static ActionSuggestion fromAIAction(long gameStateId, String gameId, AIAction aiAction) {
        ActionSuggestion suggestion = new ActionSuggestion();
        suggestion.setGameStateId(gameStateId);
        suggestion.setGameId(gameId);
        suggestion.setActionType(aiAction.getActionType());
        suggestion.setX(aiAction.getX());
        suggestion.setY(aiAction.getY());
        suggestion.setEndX(aiAction.getEndX());
        suggestion.setEndY(aiAction.getEndY());
        suggestion.setDuration(aiAction.getDuration());
        suggestion.setText(aiAction.getText());
        suggestion.setConfidence(aiAction.getConfidence());
        return suggestion;
    }
}
