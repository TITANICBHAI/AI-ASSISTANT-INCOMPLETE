package com.aiassistant.learning.model;

/**
 * Represents an action that can be taken by the AI system.
 */
public class AIAction {
    
    /**
     * Enum defining the types of actions the AI can take.
     */
    public enum ActionType {
        SUGGESTION,
        RECOMMENDATION,
        ANALYSIS,
        COMMAND_EXECUTION,
        STATE_CHANGE,
        INFORMATION_RETRIEVAL,
        LEARNING_ACTIVITY,
        COMMUNICATION,
        OBSERVATION,
        ENVIRONMENTAL_INTERACTION
    }
    
    private final String id;
    private final String name;
    private final String description;
    private final ActionType actionType;
    private final double confidence;
    
    /**
     * Constructor
     */
    public AIAction(String id, String name, String description, ActionType actionType, double confidence) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.actionType = actionType;
        this.confidence = confidence;
    }
    
    /**
     * Get action ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get action name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get action description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get action type
     */
    public ActionType getActionType() {
        return actionType;
    }
    
    /**
     * Get confidence level
     */
    public double getConfidence() {
        return confidence;
    }
    
    @Override
    public String toString() {
        return "AIAction{" +
                "name='" + name + '\'' +
                ", type=" + actionType +
                ", confidence=" + confidence +
                '}';
    }
}
