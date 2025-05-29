package com.aiassistant.core.ai;

/**
 * Enum representing different operation modes for the AI agent
 */
public enum AIAgentMode {
    /**
     * AI is fully autonomous and takes control of actions
     */
    AUTO,
    
    /**
     * AI provides suggestions but user maintains control
     */
    COPILOT;
    
    /**
     * Get a description of the mode for display to users
     * @return User-friendly description of the mode
     */
    public String getDescription() {
        switch (this) {
            case AUTO:
                return "Autonomous Mode - AI takes full control and operates independently";
            case COPILOT:
                return "Copilot Mode - AI offers assistance while you maintain control";
            default:
                return "Unknown Mode";
        }
    }
    
    /**
     * Check if the mode allows autonomous actions
     * @return true if autonomous actions are allowed, false otherwise
     */
    public boolean allowsAutonomousActions() {
        return this == AUTO;
    }
    
    /**
     * Parse a string mode name into an AIAgentMode
     * @param modeName The name of the mode
     * @return The corresponding AIAgentMode, or COPILOT as default
     */
    public static AIAgentMode fromString(String modeName) {
        try {
            return AIAgentMode.valueOf(modeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COPILOT; // Default to COPILOT if invalid
        }
    }
}
