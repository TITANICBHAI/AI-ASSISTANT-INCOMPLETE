package com.aiassistant.feedback;

/**
 * Enum defining the different feedback modes available in the system.
 */
public enum FeedbackMode {
    /**
     * Automatic feedback mode where the system autonomously evaluates and provides feedback
     * based on observed interactions and outcomes without user intervention.
     */
    AUTO,
    
    /**
     * Manual feedback mode where explicit feedback is required from the user or the copilot
     * system. This provides more precise learning but requires more user engagement.
     */
    MANUAL,
    
    /**
     * Disabled feedback mode where no feedback is collected or processed.
     * This is useful for pure inference without learning.
     */
    DISABLED
}
