package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;

/**
 * Model class for game actions
 */
@Entity(tableName = "game_actions")
@TypeConverters(Converters.class)
public class GameAction {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private Date timestamp;
    private int actionType;
    private float x;
    private float y;
    private float endX;
    private float endY;
    private long duration;
    private String targetElementId;
    
    /**
     * Enum for action types
     */
    public enum ActionType {
        UNKNOWN(0),
        TAP(1),
        SWIPE(2),
        LONG_PRESS(3),
        MULTI_TOUCH(4),
        DRAG(5),
        PINCH(6),
        ZOOM(7),
        ROTATE(8);
        
        private final int value;
        
        ActionType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static ActionType fromValue(int value) {
            for (ActionType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
    
    public GameAction() {
    }
    
    public GameAction(int actionType, float x, float y) {
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.timestamp = new Date();
    }
    
    public GameAction(int actionType, float x, float y, float endX, float endY) {
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.timestamp = new Date();
    }
    
    // Getters and Setters
    
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
    
    public String getTargetElementId() {
        return targetElementId;
    }
    
    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }
    
    public ActionType getActionTypeEnum() {
        return ActionType.fromValue(actionType);
    }
    
    public void setActionTypeEnum(ActionType actionType) {
        this.actionType = actionType.getValue();
    }
}
