package com.aiassistant.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Represents a suggested action for the user to take based on the current game state.
 * Includes information about the action, its expected outcome, and confidence level.
 */
@Entity(tableName = "action_suggestions")
public class ActionSuggestion {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // Action information
    private String action;
    private String description;
    
    // Confidence and feedback
    private float confidence;
    private float autoFeedback;  // -1.0 to 1.0, where -1 is negative, 0 is neutral, 1 is positive
    private Boolean manualFeedback;  // true for positive, false for negative, null for none
    
    // Context info
    private long gameStateId;
    private long timestamp;
    
    /**
     * Default constructor
     */
    public ActionSuggestion() {
        this.confidence = 0.0f;
        this.autoFeedback = 0.0f;
        this.manualFeedback = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with action and description
     * @param action The action
     * @param description The description
     */
    @Ignore
    public ActionSuggestion(String action, String description) {
        this();
        this.action = action;
        this.description = description;
    }
    
    /**
     * Get the suggestion ID
     * @return The ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set the suggestion ID
     * @param id The ID to set
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get the action
     * @return The action
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Set the action
     * @param action The action to set
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Get the description
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the confidence level
     * @return The confidence level
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set the confidence level
     * @param confidence The confidence level to set
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Get the auto feedback value
     * @return The auto feedback value
     */
    public float getAutoFeedback() {
        return autoFeedback;
    }
    
    /**
     * Set the auto feedback value
     * @param autoFeedback The auto feedback value to set
     */
    public void setAutoFeedback(float autoFeedback) {
        this.autoFeedback = autoFeedback;
    }
    
    /**
     * Get the manual feedback
     * @return true for positive, false for negative, null for none
     */
    public Boolean getManualFeedback() {
        return manualFeedback;
    }
    
    /**
     * Set the manual feedback
     * @param manualFeedback true for positive, false for negative, null for none
     */
    public void setManualFeedback(Boolean manualFeedback) {
        this.manualFeedback = manualFeedback;
    }
    
    /**
     * Get the game state ID
     * @return The game state ID
     */
    public long getGameStateId() {
        return gameStateId;
    }
    
    /**
     * Set the game state ID
     * @param gameStateId The game state ID to set
     */
    public void setGameStateId(long gameStateId) {
        this.gameStateId = gameStateId;
    }
    
    /**
     * Get the timestamp
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets overall feedback score combining auto and manual feedback
     * @return Combined feedback score from -1.0 to 1.0
     */
    public float getCombinedFeedbackScore() {
        // If manual feedback exists, it takes precedence
        if (manualFeedback != null) {
            return manualFeedback ? 1.0f : -1.0f;
        }
        
        // Otherwise use auto feedback
        return autoFeedback;
    }
    
    /**
     * Checks if this suggestion has any feedback
     * @return true if it has either auto or manual feedback
     */
    public boolean hasFeedback() {
        return manualFeedback != null || autoFeedback != 0.0f;
    }
    
    @Override
    public String toString() {
        return "ActionSuggestion{" +
                "action='" + action + '\'' +
                ", description='" + description + '\'' +
                ", confidence=" + confidence +
                ", feedback=" + getCombinedFeedbackScore() +
                '}';
    }
}
