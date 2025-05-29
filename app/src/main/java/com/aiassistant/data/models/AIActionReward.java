package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a reward for an AI action
 */
@Entity(tableName = "ai_action_rewards",
        foreignKeys = @ForeignKey(
                entity = AIAction.class,
                parentColumns = "id",
                childColumns = "actionId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("actionId")}
)
public class AIActionReward {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long actionId;
    private float value;
    private long timestamp;
    private String source; // Where this reward came from (e.g., user feedback, automatic)
    private String context; // Additional context about the reward
    
    /**
     * Default constructor
     */
    public AIActionReward() {
        this.timestamp = System.currentTimeMillis();
        this.value = 0.0f;
    }
    
    /**
     * Constructor with reward value
     * 
     * @param value The reward value
     */
    public AIActionReward(float value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with action ID and reward value
     * 
     * @param actionId The action ID
     * @param value The reward value
     */
    public AIActionReward(long actionId, float value) {
        this.actionId = actionId;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get ID
     * 
     * @return The ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set ID
     * 
     * @param id The ID
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get action ID
     * 
     * @return The action ID
     */
    public long getActionId() {
        return actionId;
    }
    
    /**
     * Set action ID
     * 
     * @param actionId The action ID
     */
    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
    
    /**
     * Get reward value
     * 
     * @return The reward value
     */
    public float getValue() {
        return value;
    }
    
    /**
     * Set reward value
     * 
     * @param value The reward value
     */
    public void setValue(float value) {
        this.value = value;
    }
    
    /**
     * Get timestamp
     * 
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set timestamp
     * 
     * @param timestamp The timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get source
     * 
     * @return The source
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Set source
     * 
     * @param source The source
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * Get context
     * 
     * @return The context
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Set context
     * 
     * @param context The context
     */
    public void setContext(String context) {
        this.context = context;
    }
    
    /**
     * Factory method for positive reward
     * 
     * @param actionId The action ID
     * @param value The reward value
     * @return The reward
     */
    public static AIActionReward createPositiveReward(long actionId, float value) {
        AIActionReward reward = new AIActionReward(actionId, Math.abs(value));
        reward.setSource("automatic");
        return reward;
    }
    
    /**
     * Factory method for negative reward
     * 
     * @param actionId The action ID
     * @param value The reward value
     * @return The reward
     */
    public static AIActionReward createNegativeReward(long actionId, float value) {
        AIActionReward reward = new AIActionReward(actionId, -Math.abs(value));
        reward.setSource("automatic");
        return reward;
    }
    
    /**
     * Factory method for user feedback reward
     * 
     * @param actionId The action ID
     * @param value The reward value
     * @return The reward
     */
    public static AIActionReward createUserFeedbackReward(long actionId, float value) {
        AIActionReward reward = new AIActionReward(actionId, value);
        reward.setSource("user_feedback");
        return reward;
    }
}
