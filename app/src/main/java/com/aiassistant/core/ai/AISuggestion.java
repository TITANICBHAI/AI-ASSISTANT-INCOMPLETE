package com.aiassistant.core.ai;

/**
 * Represents an AI-generated suggestion for user action, including
 * metadata about the suggestion source, confidence, and rationale.
 */
public class AISuggestion {
    
    private final AIAction action;
    private final String explanation;
    private final float confidence;
    private final String source;
    private int successCount;
    private long creationTime;
    
    /**
     * Constructor
     * @param action Action being suggested
     * @param explanation User-friendly explanation for the suggestion
     * @param confidence Confidence level (0-1)
     * @param source Source of the suggestion (e.g., "GameEngine", "AbstractReasoning")
     */
    public AISuggestion(AIAction action, String explanation, float confidence, String source) {
        this.action = action;
        this.explanation = explanation;
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        this.source = source;
        this.successCount = 0;
        this.creationTime = System.currentTimeMillis();
    }
    
    /**
     * Get the suggested action
     * @return Action
     */
    public AIAction getAction() {
        return action;
    }
    
    /**
     * Get the explanation for this suggestion
     * @return Explanation
     */
    public String getExplanation() {
        return explanation;
    }
    
    /**
     * Get the confidence level
     * @return Confidence (0-1)
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Get the source of this suggestion
     * @return Source
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Get the number of times this suggestion was successful
     * @return Success count
     */
    public int getSuccessCount() {
        return successCount;
    }
    
    /**
     * Increment the success count
     */
    public void incrementSuccessCount() {
        successCount++;
    }
    
    /**
     * Get the creation time
     * @return Creation time in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Set the creation time
     * @param creationTime Creation time in milliseconds
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    /**
     * Check if this suggestion is still relevant based on its age
     * @param maxAgeMs Maximum age in milliseconds
     * @return True if still relevant
     */
    public boolean isStillRelevant(long maxAgeMs) {
        long now = System.currentTimeMillis();
        return (now - creationTime) <= maxAgeMs;
    }
    
    /**
     * Get a user-friendly representation of the confidence level
     * @return String representation of confidence
     */
    public String getConfidenceString() {
        if (confidence >= 0.8f) {
            return "Very High";
        } else if (confidence >= 0.6f) {
            return "High";
        } else if (confidence >= 0.4f) {
            return "Medium";
        } else if (confidence >= 0.2f) {
            return "Low";
        } else {
            return "Very Low";
        }
    }
    
    /**
     * Get a user-friendly representation of this suggestion
     * @return User-friendly string
     */
    public String getDisplayText() {
        return action.getDescription() + ": " + explanation + " (" + getConfidenceString() + ")";
    }
    
    @Override
    public String toString() {
        return "AISuggestion{" +
               "action=" + action +
               ", explanation='" + explanation + '\'' +
               ", confidence=" + confidence +
               ", source='" + source + '\'' +
               ", successCount=" + successCount +
               '}';
    }
}
