package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Model class for feedback records
 */
@Entity(tableName = "feedback_records")
public class FeedbackRecord {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private long actionId;
    private long gameStateId;
    private int algorithmId;
    private float rating;
    private long timestamp;
    private String userComment;
    private boolean isExplicitFeedback;
    
    /**
     * Default constructor
     */
    public FeedbackRecord() {
        this.timestamp = System.currentTimeMillis();
        this.isExplicitFeedback = false;
    }
    
    /**
     * Constructor with basic info
     * 
     * @param gameId The game ID
     * @param actionId The action ID
     * @param gameStateId The game state ID
     * @param algorithmId The algorithm ID
     * @param rating The rating (-1 to 1)
     */
    public FeedbackRecord(String gameId, long actionId, long gameStateId, int algorithmId, float rating) {
        this();
        this.gameId = gameId;
        this.actionId = actionId;
        this.gameStateId = gameStateId;
        this.algorithmId = algorithmId;
        this.rating = rating;
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
    
    public long getActionId() {
        return actionId;
    }
    
    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
    
    public long getGameStateId() {
        return gameStateId;
    }
    
    public void setGameStateId(long gameStateId) {
        this.gameStateId = gameStateId;
    }
    
    public int getAlgorithmId() {
        return algorithmId;
    }
    
    public void setAlgorithmId(int algorithmId) {
        this.algorithmId = algorithmId;
    }
    
    public float getRating() {
        return rating;
    }
    
    public void setRating(float rating) {
        this.rating = rating;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserComment() {
        return userComment;
    }
    
    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }
    
    public boolean isExplicitFeedback() {
        return isExplicitFeedback;
    }
    
    public void setExplicitFeedback(boolean explicitFeedback) {
        isExplicitFeedback = explicitFeedback;
    }
}
