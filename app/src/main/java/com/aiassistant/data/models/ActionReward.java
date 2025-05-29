package com.aiassistant.data.models;

/**
 * Model class for action rewards
 */
public class ActionReward {
    
    private float value;
    private String description;
    private boolean isTerminal;
    private GameState resultState;
    private long timestamp;
    
    /**
     * Default constructor
     */
    public ActionReward() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with value
     * 
     * @param value The reward value
     */
    public ActionReward(float value) {
        this();
        this.value = value;
    }
    
    /**
     * Constructor with value and description
     * 
     * @param value The reward value
     * @param description The description
     */
    public ActionReward(float value, String description) {
        this(value);
        this.description = description;
    }
    
    /**
     * Constructor with value, description, and terminal flag
     * 
     * @param value The reward value
     * @param description The description
     * @param isTerminal Whether this is a terminal reward
     */
    public ActionReward(float value, String description, boolean isTerminal) {
        this(value, description);
        this.isTerminal = isTerminal;
    }
    
    /**
     * Get value
     * 
     * @return The value
     */
    public float getValue() {
        return value;
    }
    
    /**
     * Set value
     * 
     * @param value The value
     */
    public void setValue(float value) {
        this.value = value;
    }
    
    /**
     * Get description
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set description
     * 
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Check if this is a terminal reward
     * 
     * @return Whether this is a terminal reward
     */
    public boolean isTerminal() {
        return isTerminal;
    }
    
    /**
     * Set whether this is a terminal reward
     * 
     * @param terminal Whether this is a terminal reward
     */
    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }
    
    /**
     * Get result state
     * 
     * @return The result state
     */
    public GameState getResultState() {
        return resultState;
    }
    
    /**
     * Set result state
     * 
     * @param resultState The result state
     */
    public void setResultState(GameState resultState) {
        this.resultState = resultState;
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
     * Check if this is a positive reward
     * 
     * @return Whether this is a positive reward
     */
    public boolean isPositive() {
        return value > 0;
    }
    
    /**
     * Check if this is a negative reward
     * 
     * @return Whether this is a negative reward
     */
    public boolean isNegative() {
        return value < 0;
    }
    
    /**
     * Create a positive reward
     * 
     * @param value The value
     * @param description The description
     * @return The reward
     */
    public static ActionReward positive(float value, String description) {
        return new ActionReward(Math.abs(value), description);
    }
    
    /**
     * Create a negative reward
     * 
     * @param value The value
     * @param description The description
     * @return The reward
     */
    public static ActionReward negative(float value, String description) {
        return new ActionReward(-Math.abs(value), description);
    }
    
    /**
     * Create a terminal reward
     * 
     * @param value The value
     * @param description The description
     * @return The reward
     */
    public static ActionReward terminal(float value, String description) {
        return new ActionReward(value, description, true);
    }
}
