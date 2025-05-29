package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for storing user feedback
 */
@Entity(tableName = "user_feedback")
public class UserFeedback {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private String sessionId;
    private String actionId;
    private long timestamp;
    private int rating;
    private String comment;
    
    public UserFeedback() {
    }
    
    public UserFeedback(String gameId, String sessionId, String actionId, long timestamp, int rating) {
        this.gameId = gameId;
        this.sessionId = sessionId;
        this.actionId = actionId;
        this.timestamp = timestamp;
        this.rating = rating;
    }
    
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
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}
