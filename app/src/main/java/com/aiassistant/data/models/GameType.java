package com.aiassistant.data.models;

/**
 * Enum representing different types of games
 */
public enum GameType {
    FPS,
    MOBA,
    RPG,
    RACING,
    SPORTS,
    STRATEGY,
    CASUAL,
    UNKNOWN;
    
    /**
     * Get GameType from string
     */
    public static GameType fromString(String type) {
        if (type == null) {
            return UNKNOWN;
        }
        
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
