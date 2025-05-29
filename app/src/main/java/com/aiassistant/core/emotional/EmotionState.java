package com.aiassistant.core.emotional;

/**
 * Enum representing different emotional states that the AI can detect or express.
 * Each state corresponds to a basic emotional type for both detecting user emotions
 * and for generating appropriate emotional responses.
 */
public enum EmotionState {
    NEUTRAL("Neutral"),
    HAPPY("Happy"),
    SAD("Sad"),
    ANGRY("Angry"),
    AFRAID("Afraid"),
    SURPRISED("Surprised"),
    CONFUSED("Confused"),
    CONCERNED("Concerned"),
    CURIOUS("Curious"),
    REASSURING("Reassuring");
    
    private final String displayName;
    
    EmotionState(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get a human-readable display name for the emotion state
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this is a positive emotion
     */
    public boolean isPositive() {
        return this == HAPPY || this == CURIOUS || this == REASSURING;
    }
    
    /**
     * Check if this is a negative emotion
     */
    public boolean isNegative() {
        return this == SAD || this == ANGRY || this == AFRAID;
    }
    
    /**
     * Check if this is a neutral emotion
     */
    public boolean isNeutral() {
        return this == NEUTRAL || this == SURPRISED || this == CONFUSED || this == CONCERNED;
    }
    
    /**
     * Get the appropriate counter-emotion to respond with.
     * For example, the appropriate response to ANGRY might be CONCERNED or REASSURING.
     */
    public EmotionState getCounterEmotion() {
        switch (this) {
            case ANGRY:
                return CONCERNED;
            case AFRAID:
                return REASSURING;
            case SAD:
                return CONCERNED;
            case CONFUSED:
                return REASSURING;
            default:
                return this;
        }
    }
}
