package com.aiassistant.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.aiassistant.data.models.GameState;

import java.util.UUID;

/**
 * Entity for storing user feedback on AI actions
 */
@Entity(tableName = "feedback_records")
public class FeedbackRecord {
    
    // Feedback types
    public static final int TYPE_POSITIVE = 1;
    public static final int TYPE_NEGATIVE = 2;
    public static final int TYPE_CORRECTION = 3;
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private long timestamp;
    private String gameId;
    private int feedbackType;
    private String actionTaken;
    private String userComment;
    private String suggestedAction;
    private float rating;
    private String gameState;
    
    /**
     * Default constructor
     */
    public FeedbackRecord() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.rating = 0.0f;
    }
    
    /**
     * Constructor with feedback type
     * 
     * @param feedbackType The feedback type
     */
    @Ignore
    public FeedbackRecord(int feedbackType) {
        this();
        this.feedbackType = feedbackType;
    }
    
    /**
     * Constructor with all parameters
     * 
     * @param id The ID
     * @param timestamp The timestamp
     * @param gameId The game ID
     * @param feedbackType The feedback type
     * @param actionTaken The action taken
     * @param userComment The user comment
     * @param suggestedAction The suggested action
     * @param rating The rating
     */
    @Ignore
    public FeedbackRecord(@NonNull String id, long timestamp, String gameId, int feedbackType,
                         String actionTaken, String userComment, String suggestedAction, 
                         float rating) {
        this.id = id;
        this.timestamp = timestamp;
        this.gameId = gameId;
        this.feedbackType = feedbackType;
        this.actionTaken = actionTaken;
        this.userComment = userComment;
        this.suggestedAction = suggestedAction;
        this.rating = rating;
    }
    
    /**
     * Get the ID
     * 
     * @return The ID
     */
    @NonNull
    public String getId() {
        return id;
    }
    
    /**
     * Set the ID
     * 
     * @param id The ID
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }
    
    /**
     * Get the timestamp
     * 
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp
     * 
     * @param timestamp The timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the game ID
     * 
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * Set the game ID
     * 
     * @param gameId The game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    /**
     * Get the feedback type
     * 
     * @return The feedback type
     */
    public int getFeedbackType() {
        return feedbackType;
    }
    
    /**
     * Set the feedback type
     * 
     * @param feedbackType The feedback type
     */
    public void setFeedbackType(int feedbackType) {
        this.feedbackType = feedbackType;
    }
    
    /**
     * Get the action taken
     * 
     * @return The action taken
     */
    public String getActionTaken() {
        return actionTaken;
    }
    
    /**
     * Set the action taken
     * 
     * @param actionTaken The action taken
     */
    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
    
    /**
     * Get the user comment
     * 
     * @return The user comment
     */
    public String getUserComment() {
        return userComment;
    }
    
    /**
     * Set the user comment
     * 
     * @param userComment The user comment
     */
    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }
    
    /**
     * Get the suggested action
     * 
     * @return The suggested action
     */
    public String getSuggestedAction() {
        return suggestedAction;
    }
    
    /**
     * Set the suggested action
     * 
     * @param suggestedAction The suggested action
     */
    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }
    
    /**
     * Get the rating
     * 
     * @return The rating
     */
    public float getRating() {
        return rating;
    }
    
    /**
     * Set the rating
     * 
     * @param rating The rating
     */
    public void setRating(float rating) {
        this.rating = rating;
    }
    
    /**
     * Get the game state
     * 
     * @return The game state
     */
    public String getGameState() {
        return gameState;
    }
    
    /**
     * Set the game state
     * 
     * @param gameState The game state
     */
    public void setGameState(String gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Set the game state from a GameState object
     * 
     * @param gameState The game state object
     */
    @Ignore
    public void setGameStateObject(GameState gameState) {
        // In a real implementation, this would serialize the GameState to JSON
        // For this implementation, we'll just store a placeholder
        this.gameState = "Game state at " + timestamp;
    }
    
    /**
     * Get the game state as a GameState object
     * 
     * @return The game state object or null
     */
    @Ignore
    public GameState getGameStateObject() {
        // In a real implementation, this would deserialize the JSON to a GameState
        // For this implementation, we'll return null
        return null;
    }
}
