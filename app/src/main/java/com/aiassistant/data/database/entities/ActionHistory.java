package com.aiassistant.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.database.converters.Converters;
import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.ScreenActionEntity;

/**
 * Entity for storing action history in the database
 */
@Entity(tableName = "action_history")
@TypeConverters(Converters.class)
public class ActionHistory {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private long timestamp;
    private int actionType;
    private float x;
    private float y;
    private float endX;
    private float endY;
    private long duration;
    private String text;
    private long targetElementId;
    private float confidence;
    private float reward;
    private long gameStateId;
    private boolean isAIGenerated;
    
    /**
     * Default constructor
     */
    public ActionHistory() {
    }
    
    /**
     * Convert from AIAction
     * 
     * @param action The AI action
     * @param reward The reward
     * @param gameStateId The game state ID
     * @return The entity
     */
    public static ActionHistory fromAIAction(AIAction action, float reward, long gameStateId) {
        if (action == null) {
            return null;
        }
        
        ActionHistory history = new ActionHistory();
        history.setGameId(action.getGameId());
        history.setTimestamp(System.currentTimeMillis());
        history.setActionType(action.getActionType());
        history.setX(action.getX());
        history.setY(action.getY());
        history.setEndX(action.getEndX());
        history.setEndY(action.getEndY());
        history.setDuration(action.getDuration());
        history.setText(action.getText());
        history.setTargetElementId(action.getTargetElementId());
        history.setConfidence(action.getConfidence());
        history.setReward(reward);
        history.setGameStateId(gameStateId);
        history.setAIGenerated(true);
        
        return history;
    }
    
    /**
     * Convert from ScreenAction
     * 
     * @param action The screen action
     * @param reward The reward
     * @param gameStateId The game state ID
     * @return The entity
     */
    public static ActionHistory fromScreenAction(ScreenActionEntity action, float reward, long gameStateId) {
        if (action == null) {
            return null;
        }
        
        ActionHistory history = new ActionHistory();
        history.setTimestamp(System.currentTimeMillis());
        history.setActionType(action.getActionType());
        history.setX(action.getX());
        history.setY(action.getY());
        history.setEndX(action.getEndX());
        history.setEndY(action.getEndY());
        history.setDuration(action.getDuration());
        history.setText(action.getText());
        history.setTargetElementId(action.getTargetElementId());
        history.setReward(reward);
        history.setGameStateId(gameStateId);
        history.setAIGenerated(false);
        
        return history;
    }
    
    /**
     * Convert to AIAction
     * 
     * @return The AI action
     */
    public AIAction toAIAction() {
        AIAction action = new AIAction();
        action.setGameId(gameId);
        action.setActionType(actionType);
        action.setX(x);
        action.setY(y);
        action.setEndX(endX);
        action.setEndY(endY);
        action.setDuration(duration);
        action.setText(text);
        action.setTargetElementId(targetElementId);
        action.setConfidence(confidence);
        
        return action;
    }
    
    /**
     * Convert to ScreenAction
     * 
     * @return The screen action
     */
    public ScreenActionEntity toScreenAction() {
        switch (actionType) {
            case ScreenActionEntity.ACTION_TAP:
                return new ScreenActionEntity(ScreenActionEntity.ACTION_TAP, x, y);
                
            case ScreenActionEntity.ACTION_SWIPE:
                return new ScreenActionEntity(ScreenActionEntity.ACTION_SWIPE, x, y, endX, endY, duration);
                
            case ScreenActionEntity.ACTION_LONG_PRESS:
                ScreenActionEntity longPress = new ScreenActionEntity(ScreenActionEntity.ACTION_LONG_PRESS, x, y);
                longPress.setDuration(duration);
                return longPress;
                
            case ScreenActionEntity.ACTION_TEXT_INPUT:
                return new ScreenActionEntity(ScreenActionEntity.ACTION_TEXT_INPUT, x, y, text);
                
            case ScreenActionEntity.ACTION_BACK:
                return new ScreenActionEntity(ScreenActionEntity.ACTION_BACK, 0, 0);
                
            default:
                return new ScreenActionEntity(ScreenActionEntity.ACTION_TAP, x, y);
        }
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
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
    
    public long getTargetElementId() {
        return targetElementId;
    }
    
    public void setTargetElementId(long targetElementId) {
        this.targetElementId = targetElementId;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public float getReward() {
        return reward;
    }
    
    public void setReward(float reward) {
        this.reward = reward;
    }
    
    public long getGameStateId() {
        return gameStateId;
    }
    
    public void setGameStateId(long gameStateId) {
        this.gameStateId = gameStateId;
    }
    
    public boolean isAIGenerated() {
        return isAIGenerated;
    }
    
    public void setAIGenerated(boolean AIGenerated) {
        isAIGenerated = AIGenerated;
    }
}
